package nl.fannst.smtp.server.commands.smtp;

import nl.fannst.models.accounts.BasicAccount;
import nl.fannst.models.accounts.Domain;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.session.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;
import nl.fannst.smtp.server.commands.SmtpCommandRequirement;

import java.io.IOException;

public class RcptEvent implements SmtpCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommandRequirement.requireEhloHelo(session);
        SmtpCommandRequirement.requireMail(session);
    }

    private static void sendUserNotLocal(NIOClientWrapperArgument client) throws IOException {
        new SmtpReply(550, SmtpReply.EnhancedStatusCode.classPermanentFailure.add(SmtpReply.EnhancedStatusCode.detBadDestinationMailboxAddress),
                "User not local.").write(client);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommand.MAIL_RCPT_Argument argument = SmtpCommand.MAIL_RCPT_Argument.parse(command.getArguments());
        if (argument.getType() != SmtpCommand.MAIL_RCPT_Argument.Type.TO) {
            throw new SmtpCommand.InvalidArgumentException("Invalid argument, TO required.");
        }

        Domain domain = Domain.get(argument.getAddress().getDomain());
        if (domain == null) {
            if (!session.getAuthenticated()) {
                sendUserNotLocal(client);
                return;
            }

            new SmtpReply(250, SmtpReply.EnhancedStatusCode.classSuccess.add(SmtpReply.EnhancedStatusCode.subMailSystemStatus)
                    .add(SmtpReply.EnhancedStatusCode.detDestinationAddressValid),
                    "OK, relay to: " + argument.getAddress().toString()).write(client);
            session.getSessionData().addRcptTo(argument.getAddress());
        } else {
            BasicAccount basicAccount = BasicAccount.find(argument.getAddress().getUsername(), argument.getAddress().getDomain());
            if (basicAccount == null) {
                sendUserNotLocal(client);
                return;
            }

            new SmtpReply(250, SmtpReply.EnhancedStatusCode.classSuccess.add(SmtpReply.EnhancedStatusCode.subMailSystemStatus)
                    .add(SmtpReply.EnhancedStatusCode.detDestinationAddressValid),
                    "OK, to: " + basicAccount.getAddress().toString()).write(client);
            session.getSessionData().addRcptTo(basicAccount);
        }

        // Sets the RCPT flag so that the client may proceed with transmitting the data
        session.setFlags(session.getFlags() | SmtpServerSession.s_RcptFlag);
    }
}
