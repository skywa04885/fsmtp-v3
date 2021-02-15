package nl.fannst.smtp.server.commands.chunking;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.server.session.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandHandler;
import nl.fannst.smtp.server.commands.SmtpCommandRequirement;

public class BDatEvent implements SmtpCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommandRequirement.requireEhloHelo(session);
        SmtpCommandRequirement.requireMail(session);
        SmtpCommandRequirement.requireEhloHelo(session);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, SmtpServerSession session, SmtpCommand command) throws Exception {
        SmtpCommand.BDAT_Argument argument = SmtpCommand.BDAT_Argument.parse(command.getArguments());

        // Updates the session, by setting the state to chunking, and the expected chunk size
        session.setExpectedChunkSize(argument.getChunkSize());
        session.setState(SmtpServerSession.State.CHUNKING);
        session.setLastChunkOfSequence(argument.getLastOfSequence());
    }
}
