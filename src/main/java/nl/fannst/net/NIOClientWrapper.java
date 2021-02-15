package nl.fannst.net;

import nl.fannst.datatypes.SegmentedBuffer;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class NIOClientWrapper {
    protected final SocketChannel m_SocketChannel;
    protected final LinkedList<ByteBuffer> m_PendingWrites;
    protected final SegmentedBuffer m_SegmentedBuffer;
    protected long m_LastEvent;
    protected boolean m_ShouldClose;
    protected Object m_Attachment;

    public NIOClientWrapper(SocketChannel socketChannel) {
        m_SocketChannel = socketChannel;
        m_PendingWrites = new LinkedList<ByteBuffer>();
        m_SegmentedBuffer = new SegmentedBuffer("\r\n");
        m_ShouldClose = false;
    }

    public void setInterestOp(SelectionKey key, int op) throws ClosedChannelException {
        Object attachment = key.attachment();
        Selector selector = key.selector();
        m_SocketChannel.register(selector, op, attachment);
    }

    public void write(SelectionKey key, ByteBuffer data) throws ClosedChannelException {
        setInterestOp(key, SelectionKey.OP_WRITE);
        m_PendingWrites.add(data);
    }

    public LinkedList<ByteBuffer> getPendingWrites() {
        return m_PendingWrites;
    }

    public void write(SelectionKey key, String data) throws ClosedChannelException {
        write(key, ByteBuffer.wrap(data.getBytes()));
    }

    public SegmentedBuffer getSegmentedBuffer() {
        return m_SegmentedBuffer;
    }

    public Object attachment() {
        return m_Attachment;
    }

    public void attach(Object attachment) {
        m_Attachment = attachment;
    }

    public void resetLastEvent() {
        m_LastEvent = System.currentTimeMillis();
    }

    public long getLastEvent() {
        return m_LastEvent;
    }

    public boolean getShouldClose() {
        return m_ShouldClose;
    }

    public void setShouldClose(boolean should) {
        m_ShouldClose = should;
    }

    public SocketChannel getSocketChannel() {
        return m_SocketChannel;
    }
}
