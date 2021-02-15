package nl.fannst.smtp.server.commands.smtp;

import nl.fannst.models.mail.MailingList;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.session.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;

import java.util.ArrayList;

public class ExpnEvent implements SmtpCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {

    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommand.EXPN_Argument argument = SmtpCommand.EXPN_Argument.parse(command.getArguments());

        // Attempts to get the mailing list, but throws error
        //  if it was not found on our server.
        MailingList mailingList = MailingList.get(argument.getMailListName());
        if (mailingList == null) {
            new SmtpReply(450, SmtpReply.EnhancedStatusCode.classPermanentFailure.add(SmtpReply.EnhancedStatusCode.detMailingListExpansionProblem),
                    "Mailing list '" + argument.getMailListName() + "' not local.").write(client);
            return;
        }

        // Writes the mailing list to the client
        ArrayList<String> lines = new ArrayList<String>();
        mailingList.getAddresses().forEach(address -> lines.add(address.toString()));
        new SmtpReply(250, lines).write(client);
    }
}
