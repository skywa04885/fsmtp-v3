package nl.fannst.imap.server.session;

import nl.fannst.models.accounts.BasicAccount;
import nl.fannst.models.mail.mailbox_v2.Mailboxes;

public class ImapSession {
    private ImapSessionState m_State;
    private BasicAccount m_Account;
    private Mailboxes m_SelectedMailbox;

    public ImapSession() {
        m_State = ImapSessionState.NOT_AUTHENTICATED;
    }

    public ImapSessionState getState() {
        return m_State;
    }

    public void setState(ImapSessionState state) {
        m_State = state;
    }

    public BasicAccount getAccount() {
        return m_Account;
    }

    public void setAccount(BasicAccount account) {
        m_Account = account;
    }

    public Mailboxes getMailboxes() {
        return m_SelectedMailbox;
    }

    public void setMailboxes(Mailboxes mailboxes) {
        m_SelectedMailbox = mailboxes;
    }
}
