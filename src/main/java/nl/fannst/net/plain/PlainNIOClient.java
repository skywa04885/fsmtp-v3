package nl.fannst.net.plain;

import nl.fannst.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class PlainNIOClient {
    private static class QueuedRegister {
        private final SocketChannel m_SocketChannel;

        private final Object m_Attachment;

        public QueuedRegister(SocketChannel socketChannel, Object attachment) {
            m_SocketChannel = socketChannel;
            m_Attachment = attachment;
        }

        public SocketChannel getSocketChannel() {
            return m_SocketChannel;
        }

        public Object getAttachment() {
            return m_Attachment;
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    protected final Selector m_Selector;
    protected final ByteBuffer m_ReadBuffer;
    protected final LinkedList<QueuedRegister> m_QueuedRegisters;

    protected final Logger m_Logger;

    /**
     * Creates new PlainNIOClient instance
     *
     * @throws IOException possible IO exception
     */
    public PlainNIOClient() throws IOException {
        m_Selector = Selector.open();
        m_ReadBuffer = ByteBuffer.allocate(1024);
        m_QueuedRegisters = new LinkedList<QueuedRegister>();

        m_Logger = new Logger("PlainNIOClient", Logger.Level.TRACE);

        new Thread(this::task).start();
    }

    /****************************************************
     * Server Methods
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
     * Closes an socket channel
     *
     * @param socketChannel the socket channel
     * @throws IOException the IO exception
     */
    private void close(SocketChannel socketChannel) throws IOException {
        socketChannel.close();
    }

    /**
     * Gets called once a key is connectable
     *
     * @param key the key
     * @throws Exception possible IO exception
     */
    private void onKeyConnectable(SelectionKey key) throws Exception {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        PlainNIOClientWrapper clientWrapper = (PlainNIOClientWrapper) key.attachment();

        // Attempts to finish the connection, if this fails
        //  we log the error, and cancel the key.
        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            if (Logger.allowTrace()) e.printStackTrace();
            close(key);
            return;
        }

        // Tells NIO that we're waiting to receive data.
        clientWrapper.setInterestOp(key, SelectionKey.OP_READ);

        // Calls the callback
        onConnect(new PlainNIOClientArgument(key, clientWrapper));
    }

    /**
     * Gets called once a key is readable.
     *
     * @param key the key
     * @throws IOException possible IO exception
     */
    private void onKeyReadable(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        PlainNIOClientWrapper client = (PlainNIOClientWrapper) key.attachment();

        int len;
        try {
            // Clears the buffer, and reads the initial chunk of data.
            m_ReadBuffer.clear();
            len = socketChannel.read(m_ReadBuffer);

            // Check if there was anything read, if not just proceed, else enter
            //  the read loop.
            if (len > 0) {
                do {
                    // Copies the received data to an byte array, so we later can turn
                    //  it into an string.
                    byte[] data = new byte[len];
                    System.arraycopy(m_ReadBuffer.array(), 0, data, 0, len);

                    // Adds the current read data to the segmented buffer.
                    client.getSegmentedBuffer().add(new String(data));

                    // Clear the read buffer, and reads the next chunk of data, if available.
                    m_ReadBuffer.clear();
                    len = socketChannel.read(m_ReadBuffer);
                } while (len > 0);
            }

        } catch (IOException e) {
            close(key);
            return;
        }

        // If there was an EOF, close the client socket, else we will
        //  call the on data callback.
        if (len == -1) close(key);
        else onData(new PlainNIOClientArgument(key, client));
    }

    /**
     * Gets called once a key is writable
     *
     * @param key the key
     * @throws IOException possible IO exception
     */
    private void onKeyWritable(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        PlainNIOClientWrapper clientWrapper = (PlainNIOClientWrapper) key.attachment();

        // Loops over all the pending writes, and writes them to
        //  the socket channel.
        LinkedList<ByteBuffer> pendingWrites = clientWrapper.getPendingWrites();
        while (!pendingWrites.isEmpty()) {
            // Gets the pending write, and writes it to the
            //  client socket channel.
            ByteBuffer byteBuffer = pendingWrites.get(0);
            socketChannel.write(byteBuffer);

            // If more data in buffer, break to prevent block the loop
            //  else remove the empty buffer.
            if (byteBuffer.remaining() > 0) break;
            else pendingWrites.remove(0);
        }

        // If there is no further pending writes, tell NIO that
        //  we're listening for data again.
        if (pendingWrites.isEmpty()) clientWrapper.setInterestOp(key, SelectionKey.OP_READ);

        // Checks if the client should close, if so.. Close
        if (clientWrapper.getShouldClose()) close(key);
    }

    /****************************************************
     * Socket Events
     ****************************************************/

    protected abstract void onConnect(PlainNIOClientArgument client);
    protected abstract void onData(PlainNIOClientArgument client);

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Registers an new client in the event loop
     *
     * @param socketChannel the socket channel to register
     * @param attachment the attachment
     */
    protected void register(SocketChannel socketChannel, Object attachment) {
        synchronized (m_QueuedRegisters) {
            PlainNIOClientWrapper client = new PlainNIOClientWrapper(socketChannel, attachment);
            m_QueuedRegisters.add(new QueuedRegister(socketChannel, client));
        }
    }

    /****************************************************
     * Tasks
     ****************************************************/

    /**
     * The event loop for the socket operations
     */
    private void task() {
        while (true) {
            try {
                // Performs some atomic checks on the change requests, if there are any
                //  perform them, and than clear the change request list.
                synchronized (m_QueuedRegisters) {
                    // Loops over all the queued registers, and registers them in the event loop.
                    for (QueuedRegister changeRequest : m_QueuedRegisters)
                        changeRequest.getSocketChannel().register(m_Selector, SelectionKey.OP_ACCEPT,
                                changeRequest.getAttachment());

                    // Clears the queued registers.
                    m_QueuedRegisters.clear();
                }

                // Perform some selections, in order to check if there
                //  are any events we need to respond to.
                if (m_Selector.select(100) == 0) continue;

                // Starts looping over the selected keys, and calling the appropriate event
                //  handlers.
                Iterator<SelectionKey> selectedKeys = m_Selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        key.cancel();
                        continue;
                    }

                    try {
                        if (key.isConnectable()) {
                            onKeyConnectable(key);
                        } else if (key.isReadable()) {
                            onKeyReadable(key);
                        } else if (key.isWritable()) {
                            onKeyWritable(key);
                        }
                    } catch (Exception e) {
                        if (Logger.allowTrace()) e.printStackTrace();
                        close(key);
                    }
                }
            } catch (IOException e) {
                m_Logger.log("Exception occurred in event loop: " + e.getMessage(), Logger.Level.ERROR);
                e.printStackTrace();
            }
        }
    }
}
