package nl.fannst.net.secure;

import nl.fannst.Logger;
import nl.fannst.net.NIOClientWrapper;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.security.SecureRandom;
import java.util.*;

public abstract class NioSSLServer extends SSLNioPeer {
    public static final long TIMEOUT = 4 * 60 * 1000; /* 4 minutes */

    /****************************************************
     * Classy Stuff
     ****************************************************/

    protected final HashMap<SocketChannel, NioSSLClientWrapper> m_ClientWrappers;

    protected final Logger m_Logger;
    protected final ServerSocketChannel m_ServerSocketChannel;
    protected final SSLContext m_SSLContext;
    protected final Selector m_Selector;

    /**
     * Creates new NIOSSLServer
     *
     * @param config the SSL configuration
     * @param protocol the protocol to use
     * @param hostname the hostname to listen on
     * @param port the port to listen on
     * @throws Exception possible exception
     */
    public NioSSLServer(NioSSLServerConfig config, String protocol, String hostname, short port) throws Exception {
        m_ClientWrappers = new HashMap<SocketChannel, NioSSLClientWrapper>();
        m_Logger = new Logger("NioSSLServer-" + hostname + ':' + port);

        // Creates the SSL context and initializes it.
        m_SSLContext = SSLContext.getInstance(protocol);
        m_SSLContext.init(createKeyManagers(config.getServerKeyFile(), config.getServerStorePass(), config.getServerKeyPass()), new TrustManager[] {
                new SSLTrustManager()
        }, new SecureRandom());

        // Creates an dummy session in order to get the default buffer size
        //  this is just to prevent future allocations.
        {
            SSLSession session = m_SSLContext.createSSLEngine().getSession();

            m_AppReadableData = ByteBuffer.allocate(session.getApplicationBufferSize() + 64);
            m_AppEncryptedData = ByteBuffer.allocate(session.getPacketBufferSize() + 64);

            m_NetReadableData = ByteBuffer.allocate(session.getApplicationBufferSize() + 64);
            m_NetEncryptedData = ByteBuffer.allocate(session.getPacketBufferSize() + 64);

            session.invalidate();
        }

        // Creates the selector and server socket.
        m_Selector = SelectorProvider.provider().openSelector();
        m_ServerSocketChannel = ServerSocketChannel.open();

        // Configures the server socket, disable blocking and bind it to
        //  the specified hostname / port.
        m_ServerSocketChannel.configureBlocking(false);
        m_ServerSocketChannel.socket().bind(new InetSocketAddress(hostname, port));
        m_ServerSocketChannel.register(m_Selector, SelectionKey.OP_ACCEPT);

        // Runs the event thread.
        new Thread(this::run).start();

        // Runs the timeout timer
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (m_ClientWrappers) {
                    for (Map.Entry<SocketChannel, NioSSLClientWrapper> wrapper : m_ClientWrappers.entrySet()) {
                        long lastEvent = wrapper.getValue().getLastEvent();
                        if ((System.currentTimeMillis() - TIMEOUT) > lastEvent) {
                            onClientTimeout(wrapper.getValue());
                        }
                    }
                }
            }
        }, 1000, 1000);
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Runs the event thread ( read, accept, write )
     */
    private void run() {
        try {
            while (m_ServerSocketChannel.isOpen()) {
                m_Selector.select();

                Iterator<SelectionKey> selectedKeys = m_Selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        key.cancel();
                        continue;
                    }

                    try {
                        if (key.isAcceptable()) {
                            onAcceptable(key);
                        } else if (key.isReadable() || key.isWritable()) {
                            if (!key.channel().isOpen()) {
                                key.cancel();
                                continue;
                            }

                            if (key.isReadable()) {
                                onReadable(key, (NioSSLClientWrapper) key.attachment());
                            } else if (key.isWritable()) {
                                onWritable(key, (NioSSLClientWrapper) key.attachment());
                            }
                        }
                    } catch (Exception e) {
                        m_Logger.log("An exception occurred while handing event: " + e.getMessage());
                        e.printStackTrace();
                        key.cancel();
                    }
                }
            }
        } catch (Exception e) {
            synchronized (m_Logger) {
                m_Logger.log("Exception occurred while performing selection ( listener stopped! ): ", Logger.Level.FATAL);
                System.exit(-1);
            }
        }
    }

    /**
     * Handles the timeout event for client
     *
     * @param client the timed-out client
     */
    private void onClientTimeout(NioSSLClientWrapper client) {
        try {
            m_ClientWrappers.remove(client.getSocketChannel());

            closeSocketChannel(client.getSocketChannel(), client.getSSLEngine());
            onDisconnect(new NioSSLClientWrapperArgument(null, client));
        } catch (Exception e) {
            m_Logger.log("Exception occured while handling timeout of client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles when an key is acceptable
     *
     * @param key the key
     * @throws Exception the possible exception
     */
    private void onAcceptable(SelectionKey key) throws Exception {
        SocketChannel socketChannel = m_ServerSocketChannel.accept();
        socketChannel.configureBlocking(false);

        // Creates the SSL engine
        SSLEngine sslEngine = m_SSLContext.createSSLEngine();
        sslEngine.setUseClientMode(false);
        sslEngine.beginHandshake();

        // Performs the handshake, if it fails we close the connection.
        if (!performHandshake(socketChannel, sslEngine)) {
            closeSocketChannel(socketChannel, sslEngine);
            key.cancel();
            return;
        }

        // Creates the client wrapper, attaches it to the selector, and
        //  tells that we're interested in reading.
        NioSSLClientWrapper client = new NioSSLClientWrapper(socketChannel, sslEngine);
        m_ClientWrappers.put(client.getSocketChannel(), client);

        key.attach(client);
        client.setInterestOp(key, SelectionKey.OP_READ);

        // Calls the on connect callback
        onConnect(new NioSSLClientWrapperArgument(key, client));
    }

    /**
     * Handles the on readable event from key
     *
     * @param key the key
     * @param client the client
     * @throws Exception possible exception
     */
    protected void onReadable(SelectionKey key, NioSSLClientWrapper client) throws Exception {
        // Clears the current data in the buffer.
        m_NetEncryptedData.clear();

        // Starts reading the available data into a chunk of N bytes, this
        //  is to prevent the thread from locking up, or buffer overflows.
        int len;
        if ((len = client.getSocketChannel().read(m_NetEncryptedData)) == -1) {
            m_ClientWrappers.remove(client.getSocketChannel());
            onDisconnect(new NioSSLClientWrapperArgument(key, client));
            handleEOF(client.getSocketChannel(), client.getSSLEngine());
            key.cancel();
            return;
        }

        // Enters the read mode in the buffer, after which we decrypt the received
        //  data, and add it to the session buffer
        m_NetEncryptedData.flip();
        while (m_NetEncryptedData.hasRemaining()) {
            m_NetReadableData.clear();

            // Decrypts the received data, and checks the status ( if any errors may have occured )
            SSLEngineResult result = client.getSSLEngine().unwrap(m_NetEncryptedData, m_NetReadableData);
            switch (result.getStatus()) {
                case OK -> {
                    m_NetReadableData.flip();

                    byte[] data = new byte[m_NetReadableData.remaining()];
                    System.arraycopy(m_NetReadableData.array(), 0, data, 0, m_NetReadableData.remaining());

                    client.getSegmentedBuffer().add(new String(data));
                }

                case CLOSED -> {
                    m_ClientWrappers.remove(client.getSocketChannel());
                    onDisconnect(new NioSSLClientWrapperArgument(key, client));
                    closeSocketChannel(client.getSocketChannel(), client.getSSLEngine());
                    key.cancel();
                    return;
                }

                case BUFFER_OVERFLOW -> enlargeApplicationBuffer(client.getSSLEngine(), m_NetReadableData);
                case BUFFER_UNDERFLOW -> handleBufferUnderflow(client.getSSLEngine(), m_NetEncryptedData);

                default -> throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
            }
        }

        // Since we've got new data in the segmented buffer, call the on data
        //  event so the server can further process it.
        onData(new NioSSLClientWrapperArgument(key, client));

    }

    /**
     * Handles the key writable event
     *
     * @param key the key
     * @param client the client wrapper
     * @throws Exception possible exception
     */
    protected void onWritable(SelectionKey key, NioSSLClientWrapper client) throws Exception {
        LinkedList<ByteBuffer> pendingWrites = client.getPendingWrites();

        // Stays in loop while there are pending writes, while in loop
        //  we send the data in chunks of 512 bytes or less ( to prevent
        //  overflows )
        if (!pendingWrites.isEmpty()) {
            ByteBuffer buffer = pendingWrites.get(0);

            // Allocates memory of N bytes fot the chunk to be sent, after which we get
            //  the specified number of bytes from the buffer
            byte[] data = new byte[Math.min(buffer.remaining(), 512)];
            buffer.get(data);

            // Clears the encrypted data buffer, after which we encrypt the specified
            //  bytes, and check the result, if success write all of them to client.
            m_AppEncryptedData.clear();
            SSLEngineResult result = client.getSSLEngine().wrap(ByteBuffer.wrap(data), m_AppEncryptedData);
            switch (result.getStatus()) {
                case OK -> {
                    // Enter read mode, and write all of the chunk while
                    //  while staying in loop.
                    m_AppEncryptedData.flip();
                    while(m_AppEncryptedData.hasRemaining()) {
                        client.resetLastEvent();
                        client.getSocketChannel().write(m_AppEncryptedData);
                    }
                }

                case CLOSED -> {
                    m_ClientWrappers.remove(client.getSocketChannel());
                    onDisconnect(new NioSSLClientWrapperArgument(key, client));
                    closeSocketChannel(client.getSocketChannel(), client.getSSLEngine());
                    key.cancel();
                }

                case BUFFER_OVERFLOW -> enlargeApplicationBuffer(client.getSSLEngine(), m_AppEncryptedData);
                case BUFFER_UNDERFLOW -> handleBufferUnderflow(client.getSSLEngine(), m_AppReadableData);

                default -> {
                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            }

            // If there is more in the current buffer, just return
            //  and remove the pending write in the next round.
            if (buffer.remaining() > 0) {
                return;
            }

            pendingWrites.remove(0);
        }


        // If there are no pending writes anymore, reset the operation to read mode.
        //  this is important, otherwise we will receive nothing.
        if (pendingWrites.isEmpty()) {
            client.setInterestOp(key, SelectionKey.OP_READ);
        }

        // Since there is no more data available, check if the client is planned to be
        //  closed, and close it than.
        if (pendingWrites.isEmpty() && client.getShouldClose()) {
            m_ClientWrappers.remove(client.getSocketChannel());
            onDisconnect(new NioSSLClientWrapperArgument(key, client));
            closeSocketChannel(client.getSocketChannel(), client.getSSLEngine());
            key.cancel();
        }
    }

    /****************************************************
     * Events
     ****************************************************/

    protected abstract void onData(NioSSLClientWrapperArgument client) throws IOException;
    protected abstract void onConnect(NioSSLClientWrapperArgument client) throws IOException;
    protected abstract void onDisconnect(NioSSLClientWrapperArgument client) throws IOException;
}
