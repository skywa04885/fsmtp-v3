package nl.fannst.smtp.server.commands.smtp;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.session.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;

import java.net.InetAddress;

public class HeloEvent implements SmtpCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {

    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommand.HELO_EHLO_Argument hostnameArgument = SmtpCommand.HELO_EHLO_Argument.parse(command.getArguments());
        session.setGreetingHostname(hostnameArgument.getHostname());

        new SmtpReply(250, "OK, " + InetAddress.getLocalHost().getHostName() + " at your service, ("
                + client.getSocketChannel().socket().getInetAddress().getHostName() + ") ["
                + client.getSocketChannel().socket().getInetAddress().getHostAddress() + ':'
                + client.getSocketChannel().socket().getPort() + "]").write(client);

        // Sets the session flags so that we know the initial greeting is performed
        session.setFlags(session.getFlags() | SmtpServerSession.s_HeloEhloFlag);
    }
}
