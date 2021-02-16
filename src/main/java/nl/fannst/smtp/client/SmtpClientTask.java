package nl.fannst.smtp.client;

import nl.fannst.mime.Address;

import java.util.ArrayList;

public class SmtpClientTask {
    public static final int IGNORE_ERRORS = (1);

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final SmtpClientMessage m_Message;
    private ArrayList<Address> m_Recipients;
    private int m_Flags;

    /**
     * Creates an new SMTP client task
     * @param message the message
     * @param recipients the recipients
     */
    public SmtpClientTask(SmtpClientMessage message, ArrayList<Address> recipients, int flags) {
        m_Message = message;
        m_Recipients = recipients;
        m_Flags = flags;
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

    public void setFlag(int flag) {
        m_Flags |= flag;
    }

    public boolean isFlagSet(int flag) {
        return (m_Flags & flag) != 0;
    }

    public int getFlags() {
        return m_Flags;
    }
}
