package nl.fannst.smtp.server.commands;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.server.session.SmtpServerSession;

public interface SmtpCommandHandler {
    void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception;
    void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception;
}
