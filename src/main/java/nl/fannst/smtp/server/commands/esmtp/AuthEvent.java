package nl.fannst.smtp.server.commands.esmtp;

import nl.fannst.auth.Passwords;
import nl.fannst.auth.SASL;
import nl.fannst.datatypes.Pair;
import nl.fannst.mime.Address;
import nl.fannst.models.accounts.BasicAccount;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.session.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;
import nl.fannst.smtp.server.commands.SmtpCommandRequirement;

public class AuthEvent implements SmtpCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommandRequirement.requireEhloHelo(session);

        if (session.getAuthenticated()) {
            throw new SmtpCommand.SequenceException("AUTH already performed.");
        }
    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommand.AUTH_Argument argument = SmtpCommand.AUTH_Argument.parse(command.getArguments());

        if (argument.getMechanism() == SmtpCommand.AUTH_Argument.Mechanism.PLAIN) {
            // Parses the username and password from the base64 argument
            Pair<String, String> parsed = SASL.parsePlainBase64(argument.getArgument());
            String user = parsed.getFirst(), pass = parsed.getSecond();

            // Attempts to parse the address, but throws argument exception if the
            //  user does not contain valid address
            Address address;
            try {
                address = new Address(user);
            } catch (Address.InvalidAddressException e) {
                throw new SmtpCommand.InvalidArgumentException("Invalid address: " + e.getMessage());
            }

            // Gets the user from the database, and sends an error if the user is not found
            BasicAccount account = BasicAccount.find(address.getUsername(), address.getDomain());
            if (account == null) {
                new SmtpReply(454, SmtpReply.EnhancedStatusCode.classPersistentTransientFailure.add(SmtpReply.EnhancedStatusCode.subSecurityOrPolicyStatus),
                        "User not local.").write(client);
                return;
            }

            // Validates the password, if not valid send error
            if (!Passwords.verify(account.getPassword(), pass)) {
                new SmtpReply(535, SmtpReply.EnhancedStatusCode.classPermanentFailure.add(SmtpReply.EnhancedStatusCode.detAuthenticationCredentialsInvalid),
                        "Credentials invalid.").write(client);
                return;
            }

            // Sends the reply which indicates that the authentication was successful, and sets
            //  the user in the session
            new SmtpReply(235, SmtpReply.EnhancedStatusCode.classSuccess.add(SmtpReply.EnhancedStatusCode.subSecurityOrPolicyStatus),
                    "Authentication successful, welcome " + account.getFulLName()).write(client);

            session.setAuthenticatedUser(account);
        }
    }
}
