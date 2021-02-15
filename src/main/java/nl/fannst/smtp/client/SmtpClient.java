package nl.fannst.smtp.client;

import nl.fannst.Logger;
import nl.fannst.mime.Address;
import nl.fannst.net.DNS;
import nl.fannst.net.plain.PlainNIOClient;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.net.plain.PlainNIOClientWrapper;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.client.transactions.smtp.*;

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
    protected void onServerConnected(PlainNIOClientArgument client) {
        SmtpClientSession session = (SmtpClientSession) client.getClientWrapper().attachment();

        // Adds the greet transaction, and sets the executed index to one
        //  to fake it being executed, and use it to handle the response.
        session.getTransactionQueue().addTransaction(new GreetTransaction());
        session.getTransactionQueue().setTransactionExecutedIndex(1);
    }

    @Override
    protected void onServerDataChunk(PlainNIOClientArgument client, byte[] data) {
        super.onServerDataChunk(client, data);

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
                if (!session.getTransactionQueue().onReply(client, session.getReply())) {
                    session.getTransactionQueue().execute(client);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        client.getClientWrapper().getSegmentedBuffer().shift();
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

        throw new PreTransmissionError("Failed to connect to any mail server.");
    }

    /**
     * Adds an message to the transmission queue
     * @param queuedMessage the message to queue
     */
    public void enqueue(SmtpClientMessage queuedMessage) {
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
                tasks.put(domain, new SmtpClientTask(queuedMessage, recipients));
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
