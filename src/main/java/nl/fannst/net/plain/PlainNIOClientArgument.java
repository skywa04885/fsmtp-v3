package nl.fannst.net.plain;

import nl.fannst.net.NIOClientWrapper;
import nl.fannst.net.NIOClientWrapperArgument;

import java.nio.channels.SelectionKey;

public class PlainNIOClientArgument extends NIOClientWrapperArgument {

    public PlainNIOClientArgument(SelectionKey selectionKey, NIOClientWrapper clientWrapper) {
        super(selectionKey, clientWrapper);
    }
}
