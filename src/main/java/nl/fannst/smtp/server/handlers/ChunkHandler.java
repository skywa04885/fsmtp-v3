package nl.fannst.smtp.server.handlers;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.session.SmtpServerSession;

import java.io.IOException;

public class ChunkHandler {
    /**
     * Handles an chunk.
     *
     * @param client the client
     * @param session the session
     * @throws IOException possible IO exception
     */
    public static void handle(NIOClientWrapperArgument client, SmtpServerSession session) throws IOException {
        // Reads the chunk from the segmented buffer.
        session.getSessionData().getMessageBody().append(client.getClientWrapper().getSegmentedBuffer()
                .read(session.getExpectedChunkSize()));

        // Sends the confirmation, and sets the session state back to command,
        //  to allow the client to send commands again.
        session.setState(SmtpServerSession.State.COMMAND);
        new SmtpReply(250, SmtpReply.EnhancedStatusCode.classSuccess,
                "OK, received " + session.getExpectedChunkSize() + " octets.").write(client);

        // If session not last of sequence return, to prevent us from
        //  processing the data any further.
        if (!session.getLastChunkOfSequence()) return;

        // Calls the finish handler, to finish and process data.
        FinishHandler.handle(client, session);
    }
}
