package nl.fannst.smtp.server.commands.smtp;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;
import nl.fannst.smtp.server.commands.SmtpCommandRequirement;

public class DataEvent implements SmtpCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommandRequirement.requireEhloHelo(session);
        SmtpCommandRequirement.requireMail(session);
        SmtpCommandRequirement.requireRcpt(session);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        // Sends to the client that he may start sending data
        new SmtpReply(354, SmtpReply.EnhancedStatusCode.classSuccess, "End data with <CR><LF>.<CR><LF>")
                .write(client);

        // Sets the session state to data, and sets the data flag
        session.setDataStartTime(System.nanoTime());
        session.setState(SmtpServerSession.State.DATA);
        session.setFlags(session.getFlags() | SmtpServerSession.s_DataFlag);
    }
}
