package nl.fannst.imap.fancy_responses;

import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;

public abstract class FancyImapResponse {
    public abstract void write(NIOClientWrapperArgument client) throws IOException;
}
