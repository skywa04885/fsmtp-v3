package nl.fannst.smtp.server.commands;

import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.server.commands.chunking.BDatEvent;
import nl.fannst.smtp.server.commands.esmtp.*;
import nl.fannst.smtp.server.commands.fannst.XAuthorEvent;
import nl.fannst.smtp.server.commands.smtp.*;

public enum SmtpCommandEvent {
    QUIT(SmtpCommand.Type.QUIT, new QuitEvent()),
    HELO(SmtpCommand.Type.HELO, new HeloEvent()),
    HELP(SmtpCommand.Type.HELP, new HelpEvent()),
    EHLO(SmtpCommand.Type.EHLO, new EhloEvent()),
    RSET(SmtpCommand.Type.RSET, new RsetEvent()),
    NOOP(SmtpCommand.Type.NOOP, new NoopEvent()),
    MAIL(SmtpCommand.Type.MAIL, new MailEvent()),
    RCPT(SmtpCommand.Type.RCPT, new RcptEvent()),
    DATA(SmtpCommand.Type.DATA, new DataEvent()),
    AUTH(SmtpCommand.Type.AUTH, new AuthEvent()),
    BDAT(SmtpCommand.Type.BDAT, new BDatEvent()),
    VRFY(SmtpCommand.Type.VRFY, new VrfyEvent()),
    EXPN(SmtpCommand.Type.EXPN, new ExpnEvent()),
    XAUTHOR(SmtpCommand.Type.XAUTHOR, new XAuthorEvent());

    private final SmtpCommand.Type m_CommandType;
    private final SmtpCommandHandler m_EventHandler;

    SmtpCommandEvent(SmtpCommand.Type commandType, SmtpCommandHandler eventHandler) {
        m_CommandType = commandType;
        m_EventHandler = eventHandler;
    }

    public SmtpCommand.Type getType() {
        return m_CommandType;
    }

    public SmtpCommandHandler getHandler() {
        return m_EventHandler;
    }

    /**
     * Gets one of our events based on the name
     * @param type the command type
     * @return the event, null if not found
     */
    public static SmtpCommandEvent get(SmtpCommand.Type type) {
        for (SmtpCommandEvent e : SmtpCommandEvent.values()) {
            if (e.getType() == type) {
                return e;
            }
        }

        return null;
    }
}
