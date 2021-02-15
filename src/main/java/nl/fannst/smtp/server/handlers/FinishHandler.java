package nl.fannst.smtp.server.handlers;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.session.SmtpServerSession;

import java.io.IOException;

public class FinishHandler {
    /**
     * Gets called when an transaction is finished, and the data needs to be processed.
     *
     * @param client the client
     * @param session the session
     * @throws IOException possible IO exception
     */
    public static void handle(NIOClientWrapperArgument client, SmtpServerSession session) throws IOException {
        try {
            // If the processing returned true ( Message accepted ), just return.
            if (session.process(client.getClientWrapper().getSocketChannel().socket().getInetAddress())) return;

            // Since processing returned false, send an error message stating that the message got rejected.
            new SmtpReply(541, SmtpReply.EnhancedStatusCode.classPermanentFailure, "Message rejected.")
                    .write(client);
        } catch (Exception e) {
            new SmtpReply(554, SmtpReply.EnhancedStatusCode.classPermanentFailure,
                    "An exception occurred while processing message: " + e.getMessage()).write(client);
        }
    }
}
