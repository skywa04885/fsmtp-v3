package nl.fannst.smtp.server.handlers;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.session.SmtpServerSession;

import java.io.IOException;

public class DataHandler {
    /**
     * Handles data
     *
     * @param client the client
     * @param session the session
     * @param line the line to handle
     * @throws IOException the possible IO Exception
     */
    public static void handle(NIOClientWrapperArgument client, SmtpServerSession session, String line) throws IOException {
        if (line.trim().equals(".")) {
            // Sets the data transmission end time, in order to calculate the speed
            //  later on in the process
            session.setDataEndTime(System.nanoTime());

            // Calculates the speed in KB/s
            int b = session.getSessionData().getMessageBody().length();
            long t = session.getDataEndTime() - session.getDataStartTime();
            float kbps = ((float) b / (float) t) * 1000000.f;

            // Sends the reply, to tell the client that we've successfully
            //  received the message
            new SmtpReply(250, SmtpReply.EnhancedStatusCode.classSuccess, "OK, transferred " + b
                    + " bytes, with speed " + (int) kbps + "KB/s").write(client);

            // Calls the finish handler, to finish and process.
            FinishHandler.handle(client, session);
        } else {
            session.getSessionData().getMessageBody().append(line);
        }
    }

}
