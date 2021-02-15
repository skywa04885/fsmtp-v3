package nl.fannst.smtp.server.commands.smtp;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;

import java.net.InetAddress;

public class RsetEvent implements SmtpCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {

    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        session.reset();
        new SmtpReply(250, SmtpReply.EnhancedStatusCode.classSuccess, "Flushed " + InetAddress.getLocalHost().getHostName() + ".").write(client);
    }
}
