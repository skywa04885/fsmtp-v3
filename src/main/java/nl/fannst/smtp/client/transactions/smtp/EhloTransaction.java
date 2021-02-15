package nl.fannst.smtp.client.transactions.smtp;

import nl.fannst.mime.Address;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.client.SmtpClientSession;
import nl.fannst.smtp.client.transactions.CommandTransaction;
import nl.fannst.smtp.client.transactions.TransactionQueue;
import nl.fannst.smtp.client.transactions.chunking.BdatTransaction;
import nl.fannst.smtp.server.SmtpServerCapability;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

public class EhloTransaction extends CommandTransaction {
    public EhloTransaction(String hostname) {
        super(new SmtpCommand(SmtpCommand.Type.EHLO, new String[] {
                hostname
        }));
    }

    @Override
    public void execute(TransactionQueue queue, PlainNIOClientArgument client) throws IOException {
        super.execute(queue, client);
    }

    @Override
    public void onReply(TransactionQueue queue, PlainNIOClientArgument client, SmtpReply reply) throws TransactionException {
        SmtpClientSession session = (SmtpClientSession) client.getClientWrapper().attachment();
        SmtpClientSession.Capabilities capabilities = session.getCapabilities();

        // Checks if the server responded with success, if so
        //  just proceed, else throw error.
        if (reply.getCode() != 250) {
            throw new TransactionException(reply.getCode(), reply.getMessage());
        }

        // Creates the scanner and skips the first line, which contains nothing
        //  more than status stuff..
        Scanner scanner = new Scanner(reply.getMessage());
        if (scanner.hasNextLine()) scanner.next();

        // Starts looping over the capabilities, and sets the session's
        //  flags accordingly.
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim().toLowerCase(Locale.ROOT);

            // Parses the capability, if the capability is invalid ( not recognized )
            //  just proceed to the next one, we don't care.
            SmtpServerCapability serverCapability;
            if ((serverCapability = SmtpServerCapability.parse(line)) == null) {
                continue;
            }

            // Switches the type, and sets the flags / variables based on it, this will
            //  help us perform other steps in the transaction queue.
            switch (serverCapability.getType()) {
                case SIZE:
                    if (serverCapability.getArguments().length >= 1) {
                        try {
                            capabilities.setMaxSize(Integer.parseInt(serverCapability.getArguments()[0]));
                        } catch (NumberFormatException e) {
                            return;
                        }
                    }
                    break;
                case CHUNKING:
                    capabilities.setCapabilityFlag(SmtpClientSession.Capabilities.s_ChunkingCapabilityFlag);
                    break;
                case PIPELINING:
                    queue.setFlag(TransactionQueue.PIPELINING);
                    capabilities.setCapabilityFlag(SmtpClientSession.Capabilities.s_PipeliningCapabilityFlag);
                    break;
                case _8BIT_MIME:
                    capabilities.setCapabilityFlag(SmtpClientSession.Capabilities.s_8BitMimeFlag);
                    break;
                case ENHANCED_STATUS_CODES:
                    capabilities.setCapabilityFlag(SmtpClientSession.Capabilities.s_EnhancedStatusCodesFlag);
                    break;
                case BINARY_MIME:
                    capabilities.setCapabilityFlag(SmtpClientSession.Capabilities.s_BinaryMimeCapabilityFlag);
                    break;
                case SMTP_UTF8:
                    capabilities.setCapabilityFlag(SmtpClientSession.Capabilities.s_SmtpUTF8Flag);
                    break;
            }
        }

        queue.addTransaction(new MailTransaction(session.getTask().getMessage().getSender()));

        for (Address recipient : session.getTask().getRecipients()) {
            queue.addTransaction(new VrfyTransaction(recipient));
        }

        for (Address recipient : session.getTask().getRecipients()) {
            queue.addTransaction(new RcptTransaction(recipient));
        }

        if (session.getCapabilities().usesChunking()) {
            String total = (String) session.getTask().getMessage().getBody();

            int start = 0, end = 0;
            while (true) {
                int left = total.length() - end;

                end += Math.min(left, 128);

                byte[] chunk = total.substring(start, end).getBytes(StandardCharsets.US_ASCII);
                queue.addTransaction(new BdatTransaction(ByteBuffer.wrap(chunk), left < 128));

                if (end == total.length()) break;
                else start = end;
            }
        } else {
            queue.addTransaction(new DataAnnounceTransaction());

            String bodyString = (String) session.getTask().getMessage().getBody();
            queue.addTransaction(new DataTransaction(ByteBuffer.wrap(bodyString.getBytes(StandardCharsets.US_ASCII))));
        }

        queue.addTransaction(new QuitTransaction());
    }
}
