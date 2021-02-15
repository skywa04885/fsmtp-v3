package nl.fannst.net.plain;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class PlainNIOClient {
    private static class ChangeRequest {
        public static int REGISTER = 0;

        private final SocketChannel m_SocketChannel;
        private final int m_Type;
        private final int m_KeyOperation;
        private final Object m_Attachment;

        public ChangeRequest(SocketChannel socketChannel, int type, int keyOperation, Object attachment) {
            m_SocketChannel = socketChannel;
            m_Type = type;
            m_KeyOperation = keyOperation;
            m_Attachment = attachment;
        }

        public SocketChannel getSocketChannel() {
            return m_SocketChannel;
        }

        public Object attachment() {
            return m_Attachment;
        }

        public int getType() {
            return m_Type;
        }

        public int getKeyOperation() {
            return m_KeyOperation;
        }
}

    /****************************************************
     * Classy Stuff
     ****************************************************/

    protected final Selector m_Selector;
    protected final ByteBuffer m_ReadBuffer;
    protected final LinkedList<ChangeRequest> m_ChangeRequests;

    public PlainNIOClient() throws IOException {
        m_Selector = Selector.open();
        m_ReadBuffer = ByteBuffer.allocate(1024);
        m_ChangeRequests = new LinkedList<ChangeRequest>();

        new Thread(this::task).start();
    }

    /****************************************************
     * Key Events
     ****************************************************/

    private void onKeyConnectable(SelectionKey key) throws Exception {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        PlainNIOClientWrapper clientWrapper = (PlainNIOClientWrapper) key.attachment();

        System.out.println(clientWrapper.attachment());


        // Attempts to finish the connection, if this fails
        //  we log the error, and cancel the key.
        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
            return;
        }

        // Tells NIO that we're waiting to receive data.
        clientWrapper.setInterestOp(key, SelectionKey.OP_READ);

        // Calls the callback
        onServerConnected(new PlainNIOClientArgument(key, clientWrapper));
    }

    private void onKeyReadable(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        PlainNIOClientWrapper clientWrapper = (PlainNIOClientWrapper) key.attachment();

        int len;
        try {
            len = socketChannel.read(m_ReadBuffer);

            if (len > 0) {
                do {
                    byte[] data = new byte[len];
                    System.arraycopy(m_ReadBuffer.array(), 0, data, 0, len);
                    onServerDataChunk(new PlainNIOClientArgument(key, clientWrapper), data);

                    m_ReadBuffer.clear();
                    len = socketChannel.read(m_ReadBuffer);
                } while (len > 0);
            }

        } catch (IOException e) {
            socketChannel.close();
            key.channel();
            return;
        }

        if (len == -1) {
            socketChannel.close();
            key.cancel();
            return;
        }

        m_ReadBuffer.clear();
    }

    private void onKeyWritable(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        PlainNIOClientWrapper clientWrapper = (PlainNIOClientWrapper) key.attachment();

        LinkedList<ByteBuffer> pendingWrites = clientWrapper.getPendingWrites();
        while (!pendingWrites.isEmpty()) {
            ByteBuffer byteBuffer = pendingWrites.get(0);
            socketChannel.write(byteBuffer);

            if (byteBuffer.remaining() > 0) {
                break;
            }

            pendingWrites.remove(0);
        }

        // If there is no further pending writes, tell NIO that
        //  we're listening for data again.
        if (pendingWrites.isEmpty()) {
            clientWrapper.setInterestOp(key, SelectionKey.OP_READ);
        }

        // Checks if the client should close, if so.. Close
        if (clientWrapper.getShouldClose()) {
            socketChannel.close();
            key.channel();
        }
    }

    /****************************************************
     * Socket Events
     ****************************************************/

    protected void onServerConnected(PlainNIOClientArgument client) {

    }

    protected void onServerDataChunk(PlainNIOClientArgument client, byte[] data) {
        client.getClientWrapper().getSegmentedBuffer().add(new String(data));
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    protected void register(SocketChannel socketChannel, Object attachment) {
        synchronized (m_ChangeRequests) {
            m_ChangeRequests.add(new ChangeRequest(socketChannel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT, new PlainNIOClientWrapper(socketChannel, attachment)));
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
                synchronized (m_ChangeRequests) {
                    for (ChangeRequest changeRequest : m_ChangeRequests) {
                        if (changeRequest.getType() == ChangeRequest.REGISTER) {
                            changeRequest.getSocketChannel().register(m_Selector, changeRequest.getKeyOperation(), changeRequest.attachment());
                        }
                    }

                    m_ChangeRequests.clear();
                }

                // Perform some selections, in order to check if there
                //  are any events we need to respond to.
                if (m_Selector.select(100) == 0) {
                    continue;
                }

                // Starts looping over the selected keys, and calling the appropriate event
                //  handlers.
                Iterator<SelectionKey> selectedKeys = m_Selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
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
                        e.printStackTrace();
                        key.cancel();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
