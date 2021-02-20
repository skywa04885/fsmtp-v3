package nl.fannst.imap.server.commands;

import nl.fannst.imap.ImapCapability;
import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.server.commands.imap.CapabilityCommand;
import nl.fannst.imap.server.commands.imap.LoginCommand;
import nl.fannst.imap.server.commands.imap.LogoutCommand;
import nl.fannst.imap.server.commands.imap.NoopCommand;

public enum ImapCommandEvent {
    LOGOUT(ImapCommand.Type.LOGOUT, new LogoutCommand()),
    CAPABILITY(ImapCommand.Type.CAPABILITY, new CapabilityCommand()),
    NOOP(ImapCommand.Type.NOOP, new NoopCommand()),
    LOGIN(ImapCommand.Type.LOGIN, new LoginCommand());

    private final ImapCommand.Type m_CommandType;
    private final ImapCommandHandler m_EventHandler;

    ImapCommandEvent(ImapCommand.Type commandType, ImapCommandHandler eventHandler) {
        m_CommandType = commandType;
        m_EventHandler = eventHandler;
    }

    public ImapCommand.Type getType() {
        return m_CommandType;
    }

    public ImapCommandHandler getHandler() {
        return m_EventHandler;
    }

    public static ImapCommandEvent get(ImapCommand.Type type) {
        for (ImapCommandEvent e : ImapCommandEvent.values()) {
            if (e.getType() == type) {
                return e;
            }
        }

        return null;
    }
}
