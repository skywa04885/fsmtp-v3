package nl.fannst.pop3.server.commands;


import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.server.commands.pop3.*;

public enum PopCommandEvent {
    QUIT(PopCommand.Type.QUIT, new QuitEvent()),
    CAPA(PopCommand.Type.CAPA, new CapaEvent()),
    NOOP(PopCommand.Type.NOOP, new NoopEvent()),
    RSET(PopCommand.Type.RSET, new RsetEvent()),
    USER(PopCommand.Type.USER, new UserEvent()),
    PASS(PopCommand.Type.PASS, new PassEvent()),
    STAT(PopCommand.Type.STAT, new StatEvent()),
    LIST(PopCommand.Type.LIST, new ListEvent()),
    RETR(PopCommand.Type.RETR, new RetrEvent()),
    UIDL(PopCommand.Type.UIDL, new UidlEvent()),
    TOP(PopCommand.Type.TOP, new TopEvent()),
    LAST(PopCommand.Type.LAST, new LastEvent()),
    DELE(PopCommand.Type.DELE, new DeleEvent());

    private final PopCommand.Type m_CommandType;
    private final PopCommandHandler m_EventHandler;

    PopCommandEvent(PopCommand.Type commandType, PopCommandHandler eventHandler) {
        m_CommandType = commandType;
        m_EventHandler = eventHandler;
    }

    public PopCommand.Type getType() {
        return m_CommandType;
    }

    public PopCommandHandler getHandler() {
        return m_EventHandler;
    }

    public static PopCommandEvent get(PopCommand.Type type) {
        for (PopCommandEvent e : PopCommandEvent.values()) {
            if (e.getType() == type) {
                return e;
            }
        }

        return null;
    }
}
