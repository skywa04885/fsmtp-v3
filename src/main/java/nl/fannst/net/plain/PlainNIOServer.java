package nl.fannst.net.plain;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public abstract class PlainNIOServer {
    public static final long TIMEOUT = 4 * 60 * 1000; // 4 minutes

    /****************************************************
     * Classy Stuff
     ****************************************************/

    protected ServerSocketChannel m_ServerSocket;
    protected SelectionKey m_SelectionKey;
    protected Selector m_Selector;

    protected final HashMap<SocketChannel, PlainNIOClientWrapper> m_ClientWrappers;

    /**
     * Listens an plain text server on specified port and hostname
     *
     * @param hostname the hostname
     * @param port the port
     * @throws IOException possible failure
     */
    public PlainNIOServer(String hostname, short port) throws IOException {
        assert (hostname != null);

        // Creates the client wrappers hashmap
        m_ClientWrappers = new HashMap<SocketChannel, PlainNIOClientWrapper>();

        // Opens the selector
        m_Selector = Selector.open();

        // Creates the socket and binds
        m_ServerSocket = ServerSocketChannel.open();
        m_ServerSocket.bind(new InetSocketAddress(hostname, port));
        m_ServerSocket.configureBlocking(false);

        // Registers the selector
        m_SelectionKey = m_ServerSocket.register(m_Selector, m_ServerSocket.validOps());

        // Starts the listening thread
        Runnable handler = this::run;

        new Thread(handler).start();

        // Creates the timeout task
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (m_ClientWrappers) {
                    for (Map.Entry<SocketChannel, PlainNIOClientWrapper> entry : m_ClientWrappers.entrySet()) {
                        long last = entry.getValue().getLastEvent();
                        if ((System.currentTimeMillis() - TIMEOUT) > last) {
                            try {
                                m_ClientWrappers.remove(entry.getKey());
                                entry.getKey().close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }, 0,1000);
    }

    /**
     * Once the client is writable, try to shift out the available data.
     * @param key the key
     * @param client the client
     * @throws IOException possible exception
     */
    protected void onClientWritable(SelectionKey key, PlainNIOClientWrapper client) throws IOException {
        LinkedList<ByteBuffer> pendingWrites = client.getPendingWrites();
        while (!pendingWrites.isEmpty()) {
            // Resets the clients timeout
            client.resetLastEvent();

            // Gets the current buffer to write
            ByteBuffer byteBuffer = pendingWrites.get(0);
            client.getSocketChannel().write(byteBuffer);

            if (byteBuffer.remaining() > 0) {
                break;
            }

            pendingWrites.remove(0);
        }

        // If there are no further pending writes, tell NIO that we
        //  want to read response data again.
        if (pendingWrites.isEmpty()) {
            client.setInterestOp(key, SelectionKey.OP_READ);
        }

        // Checks if the client should close, if so just close
        //  it.
        if (client.getShouldClose()) {
            onDisconnect(new PlainNIOClientArgument(key, client));
            client.getSocketChannel().close();
        }
    }

    /****************************************************
     * Server Stuff
     ****************************************************/

    /**
     * Handles an key
     * @param key the key
     * @throws IOException possible exception
     */
    private void handleKey(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            SocketChannel client = m_ServerSocket.accept();
            client.configureBlocking(false);
            client.register(m_Selector, SelectionKey.OP_READ);

            // Creates the new client wrapper, and puts it in our
            //  hashmap.
            PlainNIOClientWrapper clientWrapper = new PlainNIOClientWrapper(client);
            m_ClientWrappers.put(client, clientWrapper);

            onConnect(new PlainNIOClientArgument(key, clientWrapper));
        } else if (key.isReadable()) {
            SocketChannel client = (SocketChannel) key.channel();
            if (!client.isOpen()) {
                key.cancel();
                return;
            }

            PlainNIOClientWrapper clientWrapper = m_ClientWrappers.get(client);

            // Allocates an buffer to store the received data in
            ByteBuffer buffer = ByteBuffer.allocate(512);

            // Stays in loop while there is more data available to be read
            //  since NIO will not notice another time if it is still there.
            int len;
            try {
                len = client.read(buffer);
                if (len > 0) {
                    do {
                        byte[] bytes = new byte[len];
                        System.arraycopy(buffer.array(), 0, bytes, 0, len);

                        clientWrapper.resetLastEvent();
                        clientWrapper.getSegmentedBuffer().add(new String(bytes));

                        buffer.clear();
                        len = client.read(buffer);
                    } while (len > 0);
                }
            } catch (SocketException e) {
                client.close();
                return;
            }

            // Checks if the client has an error, if so close the connection.
            if (len == -1) {
                onDisconnect(new PlainNIOClientArgument(key, clientWrapper));
                client.close();
            } else {
                clientWrapper.resetLastEvent();
                onData(new PlainNIOClientArgument(key, clientWrapper));
            }
        } else if (key.isWritable()) {
            SocketChannel client = (SocketChannel) key.channel();
            PlainNIOClientWrapper clientWrapper = m_ClientWrappers.get(client);

            // Calls the client writable event
            onClientWritable(key, clientWrapper);
        }
    }

    /**
     * Runs the plain text server
     */
    private void run() {
        try {
            while (m_ServerSocket.isOpen()) {
                m_Selector.select();

                // Gets the available keys
                Set<SelectionKey> keys = m_Selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keys.iterator();

                // Loops over the available keys
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    // If key not valid, cancel
                    if (!key.isValid()) {
                        key.cancel();
                        continue;
                    }

                    // Handles the key, and catches any exception
                    //  to prevent the server from crashing
                    try {
                        handleKey(key);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Checks if the client has closed connection, if so remove
                    //  it from the wrappers hashmap.
                    if (!key.channel().isOpen() && key.channel() instanceof SocketChannel) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        m_ClientWrappers.remove(socketChannel);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /****************************************************
     * Events
     ****************************************************/

    protected abstract void onData(PlainNIOClientArgument client) throws IOException;
    protected abstract void onConnect(PlainNIOClientArgument client) throws IOException;
    protected abstract void onDisconnect(PlainNIOClientArgument client);
}
