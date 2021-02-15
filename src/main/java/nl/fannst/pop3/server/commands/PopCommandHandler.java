package nl.fannst.pop3.server.commands;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.server.PopServerSession;

public interface PopCommandHandler {
    void allowed(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception;
    void handle(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception;
}
