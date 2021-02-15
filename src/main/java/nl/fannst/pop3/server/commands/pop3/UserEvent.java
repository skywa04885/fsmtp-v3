package nl.fannst.pop3.server.commands.pop3;

import nl.fannst.models.accounts.BasicAccount;
import nl.fannst.models.accounts.Domain;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.PopReply;
import nl.fannst.pop3.server.PopServerSession;
import nl.fannst.pop3.server.commands.PopCommandRequirement;
import nl.fannst.pop3.server.commands.PopCommandHandler;

public class UserEvent implements PopCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommandRequirement.mustBeAuthorizationState(session);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommand.USER_Argument argument = PopCommand.USER_Argument.parse(command.getArguments());

        // Attempts to get the domain
        Domain domain = Domain.get(argument.getAddress().getDomain());
        if (domain == null) {
            new PopReply(PopReply.Indicator.ERR, PopReply.ResponseCode.AUTH, "Domain not handled by this system.").write(client);
            return;
        }

        // Attempts to get the user
        BasicAccount basicAccount = BasicAccount.find(argument.getAddress().getUsername(), argument.getAddress().getDomain());
        if (basicAccount == null) {
            new PopReply(PopReply.Indicator.ERR, PopReply.ResponseCode.AUTH, "User not found.").write(client);
            return;
        }

        // Sends the reply
        new PopReply(PopReply.Indicator.OK, "Username accepted, supply password.").write(client);

        // Sets the basic account in the session
        session.setAuthenticationUser(basicAccount);
    }
}
