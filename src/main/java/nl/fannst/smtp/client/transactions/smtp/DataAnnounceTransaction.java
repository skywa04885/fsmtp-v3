package nl.fannst.smtp.client.transactions.smtp;

import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.client.transactions.CommandTransaction;
import nl.fannst.smtp.client.transactions.TransactionError;
import nl.fannst.smtp.client.transactions.TransactionQueue;

import java.io.IOException;

public class DataAnnounceTransaction extends CommandTransaction {
    public DataAnnounceTransaction() {
        super(new SmtpCommand(SmtpCommand.Type.DATA, null));
    }

    @Override
    public void execute(TransactionQueue queue, PlainNIOClientArgument client) throws IOException {
        super.execute(queue, client);
        queue.clearFlag(TransactionQueue.PIPELINING);
    }

    @Override
    public boolean onReply(TransactionQueue queue, PlainNIOClientArgument client, SmtpReply reply) throws TransactionException {
        if (reply.getCode() == 354) {
            return false;
        }

        // Since an error occurred when specifying the mail from, add it to the
        //  transaction errors array.
        queue.addTransactionError(new TransactionError(getCommand().toString(), reply.toString(false)));
        return true;
    }
}
