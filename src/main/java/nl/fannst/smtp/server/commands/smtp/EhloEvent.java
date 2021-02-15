package nl.fannst.smtp.server.commands.smtp;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.SmtpServerCapability;
import nl.fannst.smtp.server.session.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;

import java.net.InetAddress;
import java.util.ArrayList;

public class EhloEvent implements SmtpCommandHandler {
    private static final SmtpServerCapability[] s_Capabilities = {
            new SmtpServerCapability(SmtpServerCapability.Type.ENHANCED_STATUS_CODES),
            new SmtpServerCapability(SmtpServerCapability.Type.CHUNKING),
            new SmtpServerCapability(SmtpServerCapability.Type.BINARY_MIME),
            new SmtpServerCapability(SmtpServerCapability.Type.SIZE, new String[] {
                    Integer.toString(16 * 1024 * 1024),
            }),
            new SmtpServerCapability(SmtpServerCapability.Type.PIPELINING),
            new SmtpServerCapability(SmtpServerCapability.Type._8BIT_MIME),
            new SmtpServerCapability(SmtpServerCapability.Type.SMTP_UTF8),
            new SmtpServerCapability(SmtpServerCapability.Type.AUTH, new String[] {
                    "PLAIN"
            })
    };

    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {

    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommand.HELO_EHLO_Argument hostnameArgument = SmtpCommand.HELO_EHLO_Argument.parse(command.getArguments());
        session.setGreetingHostname(hostnameArgument.getHostname());

        // Creates an list of strings, which will be used to construct the EHLO message, first
        //  we create the EHLO line ( same as HELO ).
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("OK, " + InetAddress.getLocalHost().getHostName() + " at your service, ("
                + client.getSocketChannel().socket().getInetAddress().getHostName() + ") ["
                + client.getSocketChannel().socket().getInetAddress().getHostAddress() + ':'
                + client.getSocketChannel().socket().getPort() + "]");

        // Loops over the capabilities and adds the string versions to the final lines
        for (SmtpServerCapability capability : s_Capabilities) {
            lines.add(capability.toString());
        }

        // Writes the reply
        new SmtpReply(250, lines).write(client);

        // Sets the session flags so that we know the initial greeting is performed
        session.setFlags(session.getFlags() | SmtpServerSession.s_HeloEhloFlag);
    }
}
