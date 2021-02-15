package nl.fannst.smtp.client.transactions.smtp;

import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.client.transactions.CommandTransaction;
import nl.fannst.smtp.client.transactions.TransactionQueue;

import java.io.IOException;

public class HeloTransaction extends CommandTransaction {
    public HeloTransaction(String hostname) {
        super(new SmtpCommand(SmtpCommand.Type.HELO, new String[] {
                hostname
        }));
    }

    @Override
    public void execute(TransactionQueue queue, PlainNIOClientArgument client) throws IOException {
        super.execute(queue, client);
    }

    @Override
    public void onReply(TransactionQueue queue, PlainNIOClientArgument client, SmtpReply reply) throws TransactionException {
        if (reply.getCode() != 250) {
            throw new TransactionException(reply.getCode(), reply.getMessage());
        }
    }
}
