package nl.fannst.net;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NIOClientWrapperArgument {
    protected final SelectionKey m_SelectionKey;
    protected final NIOClientWrapper m_ClientWrapper;

    public NIOClientWrapperArgument(SelectionKey selectionKey, NIOClientWrapper clientWrapper) {
        m_SelectionKey = selectionKey;
        m_ClientWrapper = clientWrapper;
    }

    public SelectionKey getSelectionKey() {
        return m_SelectionKey;
    }

    public NIOClientWrapper getClientWrapper() {
        return m_ClientWrapper;
    }

    public void write(String data) throws ClosedChannelException {
        m_ClientWrapper.write(m_SelectionKey, data);
    }

    public void write(ByteBuffer buffer) throws ClosedChannelException {
        m_ClientWrapper.write(m_SelectionKey, buffer);
    }

    public SocketChannel getSocketChannel() {
        return m_ClientWrapper.getSocketChannel();
    }

    public void close() {
        m_ClientWrapper.setShouldClose(true);
    }
}
