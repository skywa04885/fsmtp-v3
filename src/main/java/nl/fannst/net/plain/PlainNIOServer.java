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
    public static final long TIMEOUT = 10 * 60 * 1000; // 10 minutes

    /****************************************************
     * Classy Stuff
     ****************************************************/

    protected final ByteBuffer m_ReadBuffer;

    protected final ServerSocketChannel m_ServerSocket;
    protected final SelectionKey m_SelectionKey;
    protected final Selector m_Selector;

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

        // Creates the buffers
        m_ReadBuffer = ByteBuffer.allocate(1024);

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

    /****************************************************
     * Server Stuff
     ****************************************************/

    /**
     * Closes an socket, and cancels the key
     *
     * @param key the key to cancel
     * @throws IOException the possible exception
     */
    private void close(SelectionKey key) throws IOException {
        assert key.channel() instanceof SocketChannel;

        close((SocketChannel) key.channel());
        key.cancel();
    }

    /**
     * Closes an socket channel, removes it from the client wrappers, and calls the disconnect callback
     *
     * @param socketChannel the socket channel
     * @throws IOException the IO exception
     */
    private void close(SocketChannel socketChannel) throws IOException {
        synchronized(m_ClientWrappers) {
            try {
                onDisconnect(new PlainNIOClientArgument(null, m_ClientWrappers.get(socketChannel)));
            } catch (Exception ignore) {}

            m_ClientWrappers.remove(socketChannel);
            socketChannel.close();
        }
    }

    /**
     * Gets called when an key is ready to be accepted
     *
     * @param key the key
     * @throws IOException possible IO exception
     */
    private void onIsAcceptable(SelectionKey key) throws IOException {
        // Accepts the client from the server socket, configures it to
        //  be non-blocking.
        SocketChannel client = m_ServerSocket.accept();
        client.configureBlocking(false);

        // Creates the new client wrapper, puts it in the hashmap ( used for timeouts )
        //  and sets the operation to read,
        PlainNIOClientWrapper clientWrapper = new PlainNIOClientWrapper(client);
        clientWrapper.setInterestOp(key, SelectionKey.OP_READ);

        synchronized (m_ClientWrappers) {
            m_ClientWrappers.put(client, clientWrapper);
        }

        // Calls the connect event
        onConnect(new PlainNIOClientArgument(key, clientWrapper));
    }

    /**
     * Gets called when key readable
     *
     * @param key the selection key
     * @throws IOException possible IO exception
     */
    private void onIsReadable(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        if (!socketChannel.isOpen()) {
            close(key);
            return;
        }

        synchronized (m_ClientWrappers) {
            PlainNIOClientWrapper clientWrapper = m_ClientWrappers.get(socketChannel);

            // Stays in loop while there is more data available to be read
            //  since NIO will not notice another time if it is still there.
            int len;
            try {
                m_ReadBuffer.clear();

                // Resets the last event, and reads the first set of bytes.
                clientWrapper.resetLastEvent();
                len = socketChannel.read(m_ReadBuffer);

                // If there are more bytes left, read them too.
                if (len > 0) {
                    do {
                        byte[] bytes = new byte[len];
                        System.arraycopy(m_ReadBuffer.array(), 0, bytes, 0, len);

                        // Resets the last event, and adds the currently received
                        //  string to the segmented buffers.
                        clientWrapper.resetLastEvent();
                        clientWrapper.getSegmentedBuffer().add(new String(bytes));

                        m_ReadBuffer.clear();
                        len = socketChannel.read(m_ReadBuffer);
                    } while (len > 0);
                }
            } catch (SocketException e) {
                close(key);
                return;
            }

            // If we've reached an EOF, close the socket, else call
            //  the on data event.
            if (len == -1) close(key);
            else onData(new PlainNIOClientArgument(key, clientWrapper));
        }
    }

    /**
     * Once the client is writable, try to shift out the available data.
     *
     * @param key the key
     * @throws IOException possible exception
     */
    protected void onClientWritable(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        if (!socketChannel.isOpen()) {
            close(key);
            return;
        }

        synchronized (m_ClientWrappers) {
            PlainNIOClientWrapper client = m_ClientWrappers.get(socketChannel);

            // Gets all the pending writes, and stays in loop while there are more available.
            LinkedList<ByteBuffer> pendingWrites = client.getPendingWrites();
            while (!pendingWrites.isEmpty()) {
                ByteBuffer byteBuffer = pendingWrites.get(0);

                // Writes the data to the socket channel, and resets the client timeout.
                client.getSocketChannel().write(byteBuffer);
                client.resetLastEvent();

                // If there is more data in the buffer, break to prevent freeze of thread
                //  else remove the empty buffer.
                if (byteBuffer.remaining() > 0) break;
                else pendingWrites.remove(0);
            }

            // If there are no pending writes anymore, tell NIO that we want to read again.
            if (pendingWrites.isEmpty()) client.setInterestOp(key, SelectionKey.OP_READ);

            // If there is any queued disconnect, perform it now.
            if (client.getShouldClose()) close(key);
        }
    }

    /**
     * Handles an key
     *
     * @param key the key
     * @throws IOException possible exception
     */
    private void handleKey(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            onIsAcceptable(key);
        } else if (key.isReadable()) {
            onIsReadable(key);
        } else if (key.isWritable()) {
            onClientWritable(key);
        }
    }

    /**
     * Runs the plain text server
     */
    private void run() {
        try {
            while (m_ServerSocket.isOpen()) {
                m_Selector.select();

                Set<SelectionKey> keys = m_Selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid()) {
                        key.cancel();
                        continue;
                    }

                    try {
                        handleKey(key);
                    } catch (Exception e) {
                        e.printStackTrace();
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
    protected abstract void onDisconnect(PlainNIOClientArgument client) throws IOException;
}
