package nl.fannst.smtp.server;

import nl.fannst.datatypes.SegmentedBuffer;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.handlers.CommandHandler;
import nl.fannst.smtp.server.handlers.DataHandler;
import nl.fannst.smtp.server.runnables.SmtpSessionInfo;
import nl.fannst.smtp.server.runnables.SmtpStoreMessage;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SMTPServer {
    /**
     * Handles data, gets called after each received packet
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
                    DataHandler.handleData(client, session, segmentedBuffer.read());
                    more = segmentedBuffer.lineAvailable();
                }
            } else if (session.getState() == SmtpServerSession.State.COMMAND) {
                if (segmentedBuffer.lineAvailable()) {
                    CommandHandler.handleCommand(client, session, segmentedBuffer.read());
                    more = segmentedBuffer.lineAvailable();
                }
            } else if (session.getState() == SmtpServerSession.State.CHUNKING) {
                if (segmentedBuffer.availableBytes() >= session.getExpectedChunkSize()) {
                    // Reads the chunk from the segmented buffer
                    session.getSessionData().getMessageBody().append(segmentedBuffer.read(session.getExpectedChunkSize()));

                    // Sends the confirmation
                    new SmtpReply(250, SmtpReply.EnhancedStatusCode.classSuccess, "OK, received " + session.getExpectedChunkSize() + " octets.").write(client);

                    // Restores the session to COMMAND state, and checks if there is a new line available
                    session.setState(SmtpServerSession.State.COMMAND);

                    // Checks if last of sequence, if so process body
                    if (session.getLastChunkOfSequence()) {
                        InetSocketAddress inetSocketAddress = (InetSocketAddress) client.getClientWrapper().getSocketChannel().getRemoteAddress();

                        SmtpSessionProtocol sessionProtocol;
                        if (session.getAuthenticated()) {
                            sessionProtocol = SmtpSessionProtocol.ESMTP_A;
                        } else {
                            sessionProtocol = SmtpSessionProtocol.ESMTP;
                        }

                        SmtpSessionInfo sessionInfo = new SmtpSessionInfo(session.getGreetingHostname(), inetSocketAddress.getAddress(),
                                sessionProtocol, (byte) 0);

                        new Thread(new SmtpStoreMessage(sessionInfo, session.getSessionData().getMessageBody().toString(),
                                session.getSessionData().getMailFrom(),
                                session.getSessionData().getRcptTo())).start();
                    }

                    // Checks if there is more available
                    more = segmentedBuffer.lineAvailable();
                } else {
                    more = false;
                }
            }
        } while (more);

        // Removes all the already-read data from the segmented buffer,
        //  ( frees memory ;p )
        segmentedBuffer.shift();
    }
}
