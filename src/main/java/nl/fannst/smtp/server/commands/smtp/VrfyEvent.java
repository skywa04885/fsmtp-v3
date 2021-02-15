package nl.fannst.smtp.server.commands.smtp;

import nl.fannst.datatypes.Pair;
import nl.fannst.models.accounts.BasicAccount;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;

import java.util.Locale;

public class VrfyEvent implements SmtpCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {

    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommand.VRFY_Argument argument = SmtpCommand.VRFY_Argument.parse(command.getArguments());

        BasicAccount account;
        boolean ambiguous;

        // Checks the type of argument, and gets the data accordingly
        if (argument.getType() == SmtpCommand.VRFY_Argument.Type.Address) {
            account = BasicAccount.find(argument.getAddress().getUsername(), argument.getAddress().getDomain());
            ambiguous = false;
        } else  {
            Pair<BasicAccount, Boolean> pair = BasicAccount.find(argument.getUsername().toLowerCase(Locale.ROOT));
            ambiguous = pair.getSecond();
            account = pair.getFirst();
        }

        // Checks how to respond
        if (account == null) {
            new SmtpReply(251, SmtpReply.EnhancedStatusCode.classPermanentFailure, "User not local.").write(client);
        } else if (ambiguous) {
            new SmtpReply(553, SmtpReply.EnhancedStatusCode.classPermanentFailure, "User ambiguous.").write(client);
        } else {
            new SmtpReply(250, SmtpReply.EnhancedStatusCode.classSuccess, account.getAddress().toString()).write(client);
        }
    }
}
