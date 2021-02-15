package nl.fannst.smtp.client;

import nl.fannst.mime.Address;

import java.util.ArrayList;

public class SmtpClientMessage {
    /****************************************************
     * Data Types
     ****************************************************/

    public static enum Type {
        DirectRelay,
        Compose
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final Address m_Sender;
    private final ArrayList<Address> m_Recipients;
    private final Object m_Body;

    /**
     * Creates new SMTP client message instance
     * @param sender the sender
     * @param recipients the recipients
     * @param body the message body / compose instructions
     */
    public SmtpClientMessage(Address sender, ArrayList<Address> recipients, String body) {
        m_Body = (Object) body;
        m_Recipients = recipients;
        m_Sender = sender;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public Address getSender() {
        return m_Sender;
    }

    public ArrayList<Address> getRecipients() {
        return m_Recipients;
    }

    public Object getBody() {
        return m_Body;
    }
}
