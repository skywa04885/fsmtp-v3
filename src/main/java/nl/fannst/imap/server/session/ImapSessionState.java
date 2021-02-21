package nl.fannst.imap.server.session;

public enum ImapSessionState {
    NOT_AUTHENTICATED,
    AUTHENTICATED,
    SELECTED,
    SELECTED_NO_CHANGES
}
