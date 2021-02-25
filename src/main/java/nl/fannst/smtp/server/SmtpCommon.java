package nl.fannst.smtp.server;

import nl.fannst.datatypes.SegmentedBuffer;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.server.handlers.ChunkHandler;
import nl.fannst.smtp.server.handlers.CommandHandler;
import nl.fannst.smtp.server.handlers.DataHandler;
import nl.fannst.smtp.server.session.SmtpServerSession;

import java.io.IOException;

public class SmtpCommon {
    /**
     * Handles data, gets called after each received packet
     *
     * @param client the client
     * @param session the session
     * @throws IOException the IO Exception
     */
    public static void handleData(NIOClientWrapperArgument client, SmtpServerSession session) throws IOException
    {
        SegmentedBuffer segmentedBuffer = client.getClientWrapper().getSegmentedBuffer();

        boolean more = false;
        do {
            if (session.getState() == SmtpServerSession.State.DATA) {
                if (segmentedBuffer.lineAvailable()) {
                    DataHandler.handle(client, session, segmentedBuffer.read());
                    more = segmentedBuffer.lineAvailable();
                }
            } else if (session.getState() == SmtpServerSession.State.COMMAND) {
                if (segmentedBuffer.lineAvailable()) {
                    CommandHandler.handle(client, session, segmentedBuffer.read());
                    more = segmentedBuffer.lineAvailable();
                }
            } else if (session.getState() == SmtpServerSession.State.CHUNKING) {
                if (segmentedBuffer.availableBytes() >= session.getExpectedChunkSize()) {
                    ChunkHandler.handle(client, session);
                    more = segmentedBuffer.lineAvailable();
                } else {
                    more = false;
                }
            }
        } while (more);

        segmentedBuffer.shift();
    }
}
