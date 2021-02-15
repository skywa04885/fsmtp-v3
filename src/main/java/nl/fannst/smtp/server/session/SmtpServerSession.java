package nl.fannst.smtp.server.session;

import nl.fannst.mime.Address;
import nl.fannst.models.accounts.BasicAccount;
import nl.fannst.smtp.MessageProcessor;
import nl.fannst.smtp.server.SmtpStoreMessage;

import java.net.InetAddress;
import java.util.ArrayList;

public class SmtpServerSession {
    public static final byte s_HeloEhloFlag = (1);
    public static final byte s_MailFlag = (1 << 1);
    public static final byte s_RcptFlag = (1 << 2);
    public static final byte s_DataFlag = (1 << 3);
    public static final byte s_DataEndFlag = (1 << 4);
    public static final byte s_StartTlsFlag = (1 << 5);

    /****************************************************
     * Data Types
     ****************************************************/

    public static enum State {
        COMMAND, DATA, CHUNKING
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final SmtpServerSessionData m_SessionData;
    private long m_Flags;
    private long m_DataStartTime, m_DataEndTime;
    private String m_GreetingHostname;
    private State m_State;

    /* CHUNKING */
    private int m_ExpectedChunkSize;
    private boolean m_LastChunkOfSequence;

    /* Authentication */
    private BasicAccount m_AuthenticatedUser;

    public SmtpServerSession() {
        m_SessionData = new SmtpServerSessionData();
        m_State = State.COMMAND;
        m_Flags = 0;
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Resets the current session
     */
    public void reset() {
        m_Flags = 0;
        m_State = State.COMMAND;
        m_SessionData.reset();
        m_ExpectedChunkSize = 0;

        m_DataStartTime = m_DataEndTime = 0;

        m_AuthenticatedUser = null;
    }

    public boolean process(InetAddress remoteAddress) throws Exception {
        MessageProcessor messageProcessor = new MessageProcessor(m_SessionData.getMessageBody().toString(),
                m_GreetingHostname, m_SessionData.getMailFrom().get(0), remoteAddress);

        if (!messageProcessor.validate(m_AuthenticatedUser != null)) return false;

        new Thread(new SmtpStoreMessage(
                messageProcessor.build(),
                (ArrayList<Address>) m_SessionData.getMailFrom().clone(),
                (ArrayList<SmtpServerSessionData.Rcpt>) m_SessionData.getRcptTo().clone()
        )).start();

        return true;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public State getState() {
        return m_State;
    }

    public void setState(State state) {
        m_State = state;
    }

    public void setFlags(long flags) {
        m_Flags = flags;
    }

    public long getFlags() {
        return m_Flags;
    }

    public SmtpServerSessionData getSessionData() {
        return m_SessionData;
    }

    public long getDataStartTime() {
        return m_DataStartTime;
    }

    public long getDataEndTime() {
        return m_DataEndTime;
    }

    public void setDataStartTime(long v) {
        m_DataStartTime = v;
    }

    public void setDataEndTime(long v) {
        m_DataEndTime = v;
    }

    public void setExpectedChunkSize(int size) {
        m_ExpectedChunkSize = size;
    }

    public int getExpectedChunkSize() {
        return m_ExpectedChunkSize;
    }

    public void setLastChunkOfSequence(boolean val) {
        m_LastChunkOfSequence = val;
    }

    public boolean getLastChunkOfSequence() {
        return m_LastChunkOfSequence;
    }

    public BasicAccount getAuthenticatedUser() {
        return m_AuthenticatedUser;
    }

    public void setAuthenticatedUser(BasicAccount account) {
        m_AuthenticatedUser = account;
    }

    public boolean getAuthenticated() {
        return m_AuthenticatedUser != null;
    }

    public void setGreetingHostname(String greetingHostname) {
        m_GreetingHostname = greetingHostname;
    }

    public String getGreetingHostname() {
        return m_GreetingHostname;
    }
}
