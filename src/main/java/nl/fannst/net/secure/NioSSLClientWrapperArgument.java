package nl.fannst.net.secure;

import nl.fannst.net.NIOClientWrapper;
import nl.fannst.net.NIOClientWrapperArgument;

import java.nio.channels.SelectionKey;

public class NioSSLClientWrapperArgument extends NIOClientWrapperArgument {
    public NioSSLClientWrapperArgument(SelectionKey selectionKey, NioSSLClientWrapper clientWrapper) {
        super(selectionKey, (NIOClientWrapper) clientWrapper);
    }
}
