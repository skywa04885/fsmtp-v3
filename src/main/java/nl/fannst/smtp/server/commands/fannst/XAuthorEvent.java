package nl.fannst.smtp.server.commands.fannst;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.session.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;
import nl.fannst.smtp.server.commands.SmtpCommandRequirement;

public class XAuthorEvent implements SmtpCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommandRequirement.requireEhloHelo(session);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        new SmtpReply(600, SmtpReply.EnhancedStatusCode.classXFannst,
                "Luke A.C.A. Rieff ( Skywa04885 )").write(client);
    }
}
