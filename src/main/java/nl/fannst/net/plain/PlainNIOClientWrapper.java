package nl.fannst.net.plain;

import nl.fannst.datatypes.SegmentedBuffer;
import nl.fannst.net.NIOClientWrapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class PlainNIOClientWrapper extends NIOClientWrapper {
    public PlainNIOClientWrapper(SocketChannel socketChannel, Object attachment) {
        super(socketChannel);

        attach(attachment);
    }

    public PlainNIOClientWrapper(SocketChannel socketChannel) {
        this(socketChannel, null);
    }
}
