package nl.fannst.smtp.server.session;

import nl.fannst.mime.Address;
import nl.fannst.models.accounts.BasicAccount;

import java.util.ArrayList;

public class SmtpServerSessionData {
    public static class Rcpt {
        private final Address m_Address;
        private final BasicAccount m_Account;

        public Rcpt(Address address, BasicAccount account) {
            m_Address = address;
            m_Account = account;
        }

        public Rcpt(Address address) {
            this(address, null);
        }

        public Rcpt(BasicAccount account) {
            this(null, account);
        }

        public Address getAddress() {
            return m_Address;
        }

        public BasicAccount getAccount() {
            return m_Account;
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    ArrayList<Address> m_MailFrom;
    ArrayList<Rcpt> m_RcptTo;
    StringBuilder m_MessageBody;

    public SmtpServerSessionData()
    {
        m_MailFrom = new ArrayList<Address>();
        m_RcptTo = new ArrayList<Rcpt>();
        m_MessageBody = new StringBuilder();
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    public void reset() {
        m_MessageBody = null;
        m_MailFrom.clear();
        m_RcptTo.clear();
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public ArrayList<Address> getMailFrom() {
        return m_MailFrom;
    }

    public ArrayList<Rcpt> getRcptTo() {
        return m_RcptTo;
    }

    public StringBuilder getMessageBody() {
        return m_MessageBody;
    }

    public void addMailFrom(Address address) {
        m_MailFrom.add(address);
    }

    public void addRcptTo(Address address) {
        m_RcptTo.add(new Rcpt(address));
    }

    public void addRcptTo(BasicAccount account) {
        m_RcptTo.add(new Rcpt(account));
    }

    public void setMessageBody(StringBuilder body) {
        m_MessageBody = body;
    }
}
