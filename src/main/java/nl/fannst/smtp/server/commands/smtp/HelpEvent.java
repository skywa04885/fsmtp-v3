package nl.fannst.smtp.server.commands.smtp;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;

public class HelpEvent implements SmtpCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {

    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("OK (");

        for (SmtpCommand.Type type : SmtpCommand.Type.values()) {
            stringBuilder.append(' ').append(type.toString());
        }

        stringBuilder.append(" ) Fannst ESMTP Mail server: https://fannst.nl/mail");
        new SmtpReply(214, SmtpReply.EnhancedStatusCode.classSuccess, stringBuilder.toString()).write(client);
    }
}
