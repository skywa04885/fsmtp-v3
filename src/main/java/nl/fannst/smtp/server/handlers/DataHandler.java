package nl.fannst.smtp.server.handlers;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.SmtpServerSession;
import nl.fannst.smtp.server.SmtpSessionProtocol;
import nl.fannst.smtp.server.runnables.SmtpSessionInfo;
import nl.fannst.smtp.server.runnables.SmtpStoreMessage;

import java.io.IOException;
import java.net.InetSocketAddress;

public class DataHandler {
    /**
     * Handles data
     * @param client the client
     * @param session the session
     * @param line the line to handle
     * @throws IOException the possible IO Exception
     */
    public static void handleData(NIOClientWrapperArgument client, SmtpServerSession session, String line) throws IOException {
        if (line.trim().equals(".")) {
            //
            // Notifies client
            //

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
        } else {
            session.getSessionData().getMessageBody().append(line);
        }
    }

}
