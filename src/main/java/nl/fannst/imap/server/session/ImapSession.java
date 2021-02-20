package nl.fannst.imap.server.session;

public class ImapSession {
    private ImapSessionState m_State;

    public ImapSession() {
        m_State = ImapSessionState.NOT_AUTHENTICATED;
    }

    public ImapSessionState getState() {
        return m_State;
    }

    public void setState(ImapSessionState state) {
        m_State = state;
    }
}
