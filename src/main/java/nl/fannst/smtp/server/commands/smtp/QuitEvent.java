package nl.fannst.smtp.server.commands.smtp;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.session.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;

public class QuitEvent implements SmtpCommandHandler {
    private static final SmtpReply.EnhancedStatusCode s_QuitSuccessCode = new SmtpReply.EnhancedStatusCode((byte) 2, (byte) 2, (byte) 0);

    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {

    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        new SmtpReply(221, s_QuitSuccessCode, "OK, closing transmission channel.").write(client);
        client.close();
    }
}
