package nl.fannst.smtp.server.commands.smtp;

import nl.fannst.models.accounts.Domain;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.session.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;
import nl.fannst.smtp.server.commands.SmtpCommandRequirement;

public class MailEvent implements SmtpCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommandRequirement.requireEhloHelo(session);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommand.MAIL_RCPT_Argument argument = SmtpCommand.MAIL_RCPT_Argument.parse(command.getArguments());
        if (argument.getType() != SmtpCommand.MAIL_RCPT_Argument.Type.FROM) {
            throw new SmtpCommand.InvalidArgumentException("Invalid argument, FROM required.");
        }

        //
        // Validates the address
        //

        Domain domain = Domain.get(argument.getAddress().getDomain());
        if (domain != null) {
            if (!session.getAuthenticated()) {
                throw new SmtpCommand.AuthenticationRequiredException("relay requires authentication.");
            } else if (!session.getAuthenticatedUser().getUsername().equalsIgnoreCase(argument.getAddress().getUsername()) ||
                    !session.getAuthenticatedUser().getDomain().equalsIgnoreCase(argument.getAddress().getDomain())) {
                new SmtpReply(454, SmtpReply.EnhancedStatusCode.classPermanentFailure.add(SmtpReply.EnhancedStatusCode.subSecurityOrPolicyStatus),
                        "Not permitted to send from address: " + argument.getAddress().toString()).write(client);
                return;
            }
        }

        //
        // Updates session & responds
        //

        // Sets the mail address in the session
        session.getSessionData().addMailFrom(argument.getAddress());

        // Sends the reply to indicate that the mail address is valid
        new SmtpReply(250, SmtpReply.EnhancedStatusCode.classSuccess.add(SmtpReply.EnhancedStatusCode.subMailSystemStatus)
                .add(SmtpReply.EnhancedStatusCode.detDestinationAddressValid),
                "OK, from: " + argument.getAddress().toString()).write(client);

        // Sets the MAIL flag in the session, to indicate that the client may proceed with RCPT
        session.setFlags(session.getFlags() | SmtpServerSession.s_MailFlag);
    }
}
