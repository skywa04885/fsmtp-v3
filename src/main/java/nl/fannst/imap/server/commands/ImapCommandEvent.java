package nl.fannst.imap.server.commands;

import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.server.commands.imap.*;

public enum ImapCommandEvent {
    LOGOUT(ImapCommand.Type.LOGOUT, new LogoutCommand()),
    CAPABILITY(ImapCommand.Type.CAPABILITY, new CapabilityCommand()),
    NOOP(ImapCommand.Type.NOOP, new NoopCommand()),
    LOGIN(ImapCommand.Type.LOGIN, new LoginCommand()),
    SELECT(ImapCommand.Type.SELECT, new Select_Examine_Command.SelectCommand()),
    EXAMINE(ImapCommand.Type.EXAMINE, new Select_Examine_Command.ExamineCommand()),
    CREATE(ImapCommand.Type.CREATE, new CreateCommand()),
    DELETE(ImapCommand.Type.DELETE, new DeleteCommand()),
    RENAME(ImapCommand.Type.RENAME, new RenameCommand()),
    STATUS(ImapCommand.Type.STATUS, new StatusCommand());

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
