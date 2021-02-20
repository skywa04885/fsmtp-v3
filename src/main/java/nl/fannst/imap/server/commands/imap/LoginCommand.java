package nl.fannst.imap.server.commands.imap;

import nl.fannst.auth.Passwords;
import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.arguments.ImapLoginArgument;
import nl.fannst.imap.server.commands.ImapCommandHandler;
import nl.fannst.imap.server.session.ImapSession;
import nl.fannst.imap.server.session.ImapSessionState;
import nl.fannst.mime.Address;
import nl.fannst.models.accounts.BasicAccount;
import nl.fannst.models.accounts.Domain;
import nl.fannst.net.NIOClientWrapperArgument;

public class LoginCommand implements ImapCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        ImapSession session = (ImapSession) client.getClientWrapper().attachment();

        if (session.getState() != ImapSessionState.NOT_AUTHENTICATED)
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.BAD, "already logged in").write(client);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        ImapSession session = (ImapSession) client.getClientWrapper().attachment();
        ImapLoginArgument argument = (ImapLoginArgument) command.getArgument();
        Address user = argument.getUser();

        // Gets the domain, if it is not found send the client an error message,
        //  that it is not handled by this system.
        Domain domain = Domain.get(user.getDomain());
        if (domain == null) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, "domain not handled by system").write(client);
            return;
        }

        // Gets the basic account, if it is not found, send the client an error message,
        //  stating that the account is not found.
        BasicAccount basicAccount = BasicAccount.find(user.getUsername(), user.getDomain());
        if (basicAccount == null) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, "user not found").write(client);
            return;
        }

        // Validates the password, if it fails send error.
        if (!Passwords.verify(basicAccount.getPassword(), argument.getPassword())) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, "invalid password").write(client);
            return;
        }

        // Writes the success response.
        CapabilityCommand.send(client, command.getSequenceNo(), CapabilityCommand.AUTHENTICATED_CAPABILITIES);
        new ImapResponse(command.getSequenceNo(), ImapResponse.Type.OK,
                user.toString() + " authenticated").write(client);

        session.setState(ImapSessionState.AUTHENTICATED);
    }
}
