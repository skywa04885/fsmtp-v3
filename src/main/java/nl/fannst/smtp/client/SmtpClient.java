package nl.fannst.smtp.client;

import nl.fannst.Globals;
import nl.fannst.Logger;
import nl.fannst.mime.Address;
import nl.fannst.mime.ContentType;
import nl.fannst.mime.TransferEncoding;
import nl.fannst.mime.composer.ComposeTextSection;
import nl.fannst.mime.composer.Composer;
import nl.fannst.net.DNS;
import nl.fannst.net.plain.PlainNIOClient;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.net.plain.PlainNIOClientWrapper;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.client.transactions.TransactionError;
import nl.fannst.smtp.client.transactions.smtp.*;
import nl.fannst.templates.FreeWriterRenderer;

import javax.naming.NamingException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.*;

public class SmtpClient extends PlainNIOClient {
    public static class PreTransmissionError extends Exception {
        public PreTransmissionError(String m) {
            super(m);
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final Logger m_Logger;

    /* Singleton stuff */
    private static SmtpClient INSTANCE;

    private SmtpClient() throws IOException {
        super();

        m_Logger = new Logger("SMTPClient", Logger.Level.TRACE);
    }

    /****************************************************
     * Handling
     ****************************************************/

    private void handleReply(PlainNIOClientArgument client) throws Exception {
        SmtpClientSession session = (SmtpClientSession) client.getClientWrapper().attachment();
        PlainNIOClientWrapper clientWrapper = (PlainNIOClientWrapper) client.getClientWrapper();
    }

    /****************************************************
     * Socket Events
     ****************************************************/

    @Override
    protected void onConnect(PlainNIOClientArgument client) {
        SmtpClientSession session = (SmtpClientSession) client.getClientWrapper().attachment();

        // Adds the greet transaction, and sets the executed index to one
        //  to fake it being executed, and use it to handle the response.
        session.getTransactionQueue().addTransaction(new GreetTransaction());
        session.getTransactionQueue().setTransactionExecutedIndex(1);
    }

    @Override
    protected void onData(PlainNIOClientArgument client) {
        PlainNIOClientWrapper clientWrapper = (PlainNIOClientWrapper) client.getClientWrapper();
        SmtpClientSession session = (SmtpClientSession) clientWrapper.attachment();

        while (clientWrapper.getSegmentedBuffer().lineAvailable()) {
            String line = clientWrapper.getSegmentedBuffer().read();
            System.err.println("IN -> " + line.trim());

            try {
                // If there is an reply already, and it needs another line, use the received
                //  line to append to message of existing.
                if (session.getReply() != null && session.getReply().getMore()) {
                    session.getReply().parseAndAppendMessage(line.trim());
                } else {
                    session.setReply(SmtpReply.parse(line));
                }

                // If more lines are pending for read, noted by the '-', continue loop
                //  and read more.
                if (session.getReply().getMore()) {
                    continue;
                }

                // Executes the on reply handler for the pending transaction, this will return true if it was the last
                //  one, if not the last, execute the next transaction.
                switch (session.getTransactionQueue().onReply(client, session.getReply())) {
                    case Success -> session.getTransactionQueue().execute(client);

                    case Failure -> {
                        client.close();
                        sendTransactionError(session);
                    }

                    case Last -> {
                        LinkedList<TransactionError> errors = session.getTransactionQueue().getTransactionErrors();
                        if (errors.size() > 0 && !session.getTask().isFlagSet(SmtpClientTask.IGNORE_ERRORS)) {
                            try {
                                sendTransactionError(session);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        client.getClientWrapper().getSegmentedBuffer().shift();
    }

    private void sendPreTransactionError(String title, String message, Address sender) {
        try {
            // Builds the MIME Message
            //

            String body = FreeWriterRenderer.getInstance().renderPreTransactionFailure(title, message, sender);

            Composer composer = new Composer();
            composer.setSubject("Delivery Failure");
            composer.addFrom(Globals.DELIVERY_SYSTEM_ADDRESS);
            composer.addTo(new Address("luke.rieff@gmail.com", "Luke Rieff"));
            composer.addSection(new ComposeTextSection(TransferEncoding.QUOTED_PRINTABLE, ContentType.TEXT_HTML, body));

            //
            // Enqueues the message
            //

            ArrayList<Address> targets = new ArrayList<Address>();
            targets.add(sender);

            enqueue(new SmtpClientMessage(Globals.DELIVERY_SYSTEM_ADDRESS, targets, composer.compose()),
                    SmtpClientTask.IGNORE_ERRORS);
        } catch (Exception e) {

        }
    }

    private void sendTransactionError(SmtpClientSession session) throws Exception {
        LinkedList<TransactionError> errors = session.getTransactionQueue().getTransactionErrors();
        Address sender = session.getTask().getMessage().getSender();

        //
        // Builds the MIME Message
        //

        String body = FreeWriterRenderer.getInstance().renderTransactionFailure(errors, sender);

        Composer composer = new Composer();
        composer.setSubject("Delivery Failure");
        composer.addFrom(Globals.DELIVERY_SYSTEM_ADDRESS);
        composer.addTo(sender);
        composer.addSection(new ComposeTextSection(TransferEncoding.QUOTED_PRINTABLE, ContentType.TEXT_HTML, body));

        //
        // Enqueues the message
        //

        ArrayList<Address> targets = new ArrayList<Address>();
        targets.add(session.getTask().getMessage().getSender());

        enqueue(new SmtpClientMessage(Globals.DELIVERY_SYSTEM_ADDRESS, targets, composer.compose()),
                SmtpClientTask.IGNORE_ERRORS);
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    private void runTask(String domain, SmtpClientTask task) throws PreTransmissionError {
        List<DNS.MXRecord> mxRecords;

        if (Logger.allowTrace()) {
            m_Logger.log("Resolving MX Records for: " + domain);
        }

        // Attempts to get the DNS records, after which we sort them based
        //  on priority, so we can pick the recommended server.
        try {
            mxRecords = DNS.getMXRecords(domain);
            Collections.sort(mxRecords);
        } catch (NamingException e) {
            sendPreTransactionError("Failed to resolve MX records",
                    "Could not find any MX records for domain: '" + domain + "'.", task.getMessage().getSender());
            throw new PreTransmissionError("Failed to resolve MX: " + e.getMessage());
        }

        // Logs the MX records to the console
        if (Logger.allowTrace()) {
            m_Logger.log("Resolved " + mxRecords.size() + " MX Records:");
            for (DNS.MXRecord mxRecord : mxRecords) {
                m_Logger.log("\t\t{priority: " + mxRecord.getPriority() + ", server: '" + mxRecord.getValue() + "'}");
            }
        }

        // Loops over the record we've found, tries to resolve each one of them
        //  to an valid IP address, and once a valid one has been found, we create
        //  the connection, and add it to the event loop.
        for (DNS.MXRecord mxRecord : mxRecords) {
            try {
                InetAddress inetAddress = InetAddress.getByName(mxRecord.getValue());
                if (Logger.allowTrace()) {
                    m_Logger.log("Resolved domain '" + mxRecord.getValue() + "' to " + inetAddress.getHostAddress());
                }

                // Creates the socket connection to the resolved address, after which we will register
                //  it in our selector.
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                socketChannel.connect(new InetSocketAddress(inetAddress, 25));

                register(socketChannel, (Object) new SmtpClientSession(task));

                return;
            } catch (UnknownHostException e) {
                if (Logger.allowTrace()) {
                    m_Logger.log("Failed to resolve domain '" + mxRecord.getValue() + "', attempting next ..");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sendPreTransactionError("No mail servers found",
                "There were no valid mail servers for domain: '" + domain + "'.", task.getMessage().getSender());
        throw new PreTransmissionError("Failed to connect to any mail server.");
    }

    /**
     * Adds an message to the transmission queue
     * @param queuedMessage the message to queue
     */
    public void enqueue(SmtpClientMessage queuedMessage, int flags) {
        if (Logger.allowTrace()) {
            m_Logger.log("New message queued, total of " + queuedMessage.getRecipients().size() + " recipients.");
        }


        HashMap<String, SmtpClientTask> tasks = new HashMap<String, SmtpClientTask>();

        // Creates the tasks for similar domains / extensions, this will be
        //  to reduce the number of connections to be opened.
        for (Address recipient : queuedMessage.getRecipients()) {
            String domain = recipient.getDomain().toLowerCase(Locale.ROOT);

            // Checks if there is an existing task, if so add the recipient to the existing one
            //  else just create an new task, and store it in the hashmap.
            SmtpClientTask task = tasks.get(domain);
            if (task != null) {
                task.addRecipient(recipient);
            } else {
                ArrayList<Address> recipients = new ArrayList<Address>();
                recipients.add(recipient);
                tasks.put(domain, new SmtpClientTask(queuedMessage, recipients, flags));
            }
        }

        // Loops over the tasks in the hashmap, and initializes the connections.
        for (Map.Entry<String, SmtpClientTask> entry : tasks.entrySet()) {
            try {
                runTask(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                m_Logger.log("Failed to run task, error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void enqueue(SmtpClientMessage queuedMessage) {
        enqueue(queuedMessage, 0);
    }

    /****************************************************
     * Singleton Methods
     ****************************************************/

    public static void createInstance() throws IOException {
        assert INSTANCE == null;
        INSTANCE = new SmtpClient();
    }

    public static SmtpClient getInstance() {
        assert INSTANCE != null;
        return INSTANCE;
    }
}
