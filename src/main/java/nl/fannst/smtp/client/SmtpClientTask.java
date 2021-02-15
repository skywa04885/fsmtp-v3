package nl.fannst.smtp.client;

import nl.fannst.mime.Address;

import java.util.ArrayList;

public class SmtpClientTask {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final SmtpClientMessage m_Message;
    private ArrayList<Address> m_Recipients;

    /**
     * Creates an new SMTP client task
     * @param message the message
     * @param recipients the recipients
     */
    public SmtpClientTask(SmtpClientMessage message, ArrayList<Address> recipients) {
        m_Message = message;
        m_Recipients = recipients;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public SmtpClientMessage getMessage() {
        return m_Message;
    }

    public ArrayList<Address> getRecipients() {
        return m_Recipients;
    }

    public void addRecipient(Address recipient) {
        m_Recipients.add(recipient);
    }
}
