package nl.fannst.smtp.server.commands;

import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.server.session.SmtpServerSession;

public class SmtpCommandRequirement {
    public static void requireEhloHelo(SmtpServerSession session) throws SmtpCommand.SequenceException {
        if ((session.getFlags() & SmtpServerSession.s_HeloEhloFlag) == 0) {
            throw new SmtpCommand.SequenceException("HELO/EHLO first.");
        }
    }

    public static void requireMail(SmtpServerSession session) throws SmtpCommand.SequenceException {
        if ((session.getFlags() & SmtpServerSession.s_MailFlag) == 0) {
            throw new SmtpCommand.SequenceException("MAIL first.");
        }
    }

    public static void requireRcpt(SmtpServerSession session) throws SmtpCommand.SequenceException {
        if ((session.getFlags() & SmtpServerSession.s_RcptFlag) == 0) {
            throw new SmtpCommand.SequenceException("RCPT first.");
        }
    }
}
