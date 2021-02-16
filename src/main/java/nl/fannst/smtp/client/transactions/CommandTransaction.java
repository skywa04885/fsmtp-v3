package nl.fannst.smtp.client.transactions;

import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;

import java.io.IOException;

public class CommandTransaction extends Transaction {
    private final SmtpCommand m_Command;

    public CommandTransaction(SmtpCommand command) {
        super();

        m_Command = command;
    }

    @Override
    public void execute(TransactionQueue queue, PlainNIOClientArgument client) throws IOException {
        System.err.println("OUT -> " + m_Command.toString());
        m_Command.write(client);
    }

    @Override
    public boolean onReply(TransactionQueue queue, PlainNIOClientArgument client, SmtpReply reply) throws TransactionException {
        return false;
    }

    public SmtpCommand getCommand() {
        return m_Command;
    }
}
