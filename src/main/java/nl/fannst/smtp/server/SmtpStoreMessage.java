package nl.fannst.smtp.server;

import nl.fannst.Logger;
import nl.fannst.datatypes.Pair;
import nl.fannst.encryption.RSA;
import nl.fannst.mime.Address;
import nl.fannst.mime.Header;
import nl.fannst.mime.encoding.NonASCIIText;
import nl.fannst.models.accounts.BasicAccount;
import nl.fannst.models.mail.Message;
import nl.fannst.smtp.client.SmtpClient;
import nl.fannst.smtp.client.SmtpClientMessage;
import nl.fannst.smtp.server.session.SmtpServerSessionData;

import java.security.PublicKey;
import java.time.Instant;
import java.util.*;

public class SmtpStoreMessage implements Runnable {
    private final Logger m_Logger;

    private ArrayList<Header> m_Headers;
    private final String m_Message;
    private final ArrayList<Address> m_From;
    private final ArrayList<SmtpServerSessionData.Rcpt> m_To;

    private String m_Subject;
    private ArrayList<Address> m_MailFrom;
    private ArrayList<Address> m_RcptTo;

    public SmtpStoreMessage(String message, ArrayList<Address> from, ArrayList<SmtpServerSessionData.Rcpt> to) {
        super();

        m_Logger = new Logger("ProcessMessage");

        m_Message = message;
        m_From = from;
        m_To = to;
    }

    private void store(BasicAccount account) {
        m_Logger.log("Storing message for account: " + account.getAddress().toString() + " - " + account.getUUID().toString());

        PublicKey publicKey = RSA.parsePublicKey(account.getRSAPublic().getBytes());
        assert (publicKey != null);

        try {

            //
            // Gets the mailbox to store the message in.
            //

            // Gets the incoming mailbox.
            Mailbox mailbox = Mailbox.getByFlag(account.getUUID(), Mailbox.Flag.Solid.getMask() | Mailbox.Flag.Incoming.getMask());
            if (mailbox == null) {
                m_Logger.log("Incoming mailbox not found for account: " + account.getAddress().toString() + ", somethign wrong?", Logger.Level.WARN);
                return;
            }

            // Performs some debug logging.
            if (Logger.allowTrace())
                m_Logger.log("Incomming mailbox found: " + mailbox.getMailboxName() + ":" + mailbox.getMailboxID(), Logger.Level.TRACE);

            //
            // Encrypts & Stores the message.
            //

            // Encrypts the message body, and creates the message which will be stored
            //  in the database.
            Pair<byte[], byte[]> encryptedBody = Message.encryptBody(m_Message, publicKey);
            byte[] body = encryptedBody.getFirst();
            byte[] key = encryptedBody.getSecond();

            assert key != null : "Decryption key may not be null!";
            assert body != null : "Encrypted body may not be null!";

            // Creates the new message model, after which we save it to mongodb.
            Message message = new Message(Objects.requireNonNull(BasicAccount.getNextUidAndIncrement(account.getUUID())),
                    account.getUUID(),
                    mailbox.getMailboxID(),
                    m_Message.length(),
                    key,
                    body,
                    m_Subject,
                    m_MailFrom,
                    m_RcptTo,
                    0,
                    Instant.now().toEpochMilli());

            message.save();

            // Increments the message count for the specified mailbox.
            Mailbox.incMessageCount(account.getUUID(), mailbox.getMailboxID());
        } catch (Exception e) {
            m_Logger.log("Failed to store message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parses the supplied MIME message in order to get the basic info we need
     * @return valid
     */
    private boolean parseMessage() {
        try {
            m_Headers = Header.parseHeaders(new Scanner(m_Message));

            // Loops over the values, and gets the ones we need.
            for (Header header : m_Headers) {
                if (header.getKey().equals("subject")) {
                    m_Subject = NonASCIIText.decode(header.getValue());
                } else if (header.getKey().equals("from")) {
                    m_MailFrom = Address.parseAddressList(header.getValue());
                } else if (header.getKey().equals("to")) {
                    m_RcptTo = Address.parseAddressList(header.getValue());
                }
            }

            // Verifies that all required ones are there if not, just return false
            //  in order to tell the run() that the message IS NOT valid and will
            //  not be processed any further.
            if (m_MailFrom == null || m_RcptTo == null) {
                return false;
            }

            return true;
        } catch (Address.InvalidAddressException | Header.InvalidHeaderException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void run() {
        // Parses the message headers
        if (!this.parseMessage()) {
            m_Logger.log("Message does not meet requirements, voiding ...");
            return;
        }

        // Starts storage
        ArrayList<Address> transmitAddresses = new ArrayList<Address>();
        for (SmtpServerSessionData.Rcpt rcpt : m_To) {
            if (rcpt.getAccount() != null) {
                store(rcpt.getAccount());
            } else {
                transmitAddresses.add(rcpt.getAddress());
            }
        }

        if (transmitAddresses.size() >= 1) {
            SmtpClient.getInstance().enqueue(new SmtpClientMessage(m_From.get(0), transmitAddresses, m_Message));
        }
    }
}
