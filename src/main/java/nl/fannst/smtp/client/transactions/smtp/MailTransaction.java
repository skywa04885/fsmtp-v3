package nl.fannst.smtp.client.transactions.smtp;

import nl.fannst.mime.Address;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.client.transactions.CommandTransaction;
import nl.fannst.smtp.client.transactions.TransactionError;
import nl.fannst.smtp.client.transactions.TransactionQueue;

public class MailTransaction extends CommandTransaction {
    public MailTransaction(Address from) {
        super(new SmtpCommand(SmtpCommand.Type.MAIL, new String[] {
                "FROM:<" + from.getAddress() + '>'
        }));
    }

    @Override
    public boolean onReply(TransactionQueue queue, PlainNIOClientArgument client, SmtpReply reply) throws TransactionException {
        if (reply.getCode() == 250) {
            return false;
        }

        // Since an error occurred when specifying the mail from, add it to the
        //  transaction errors array.
        queue.addTransactionError(new TransactionError(getCommand().toString(), reply.toString(false)));
        return true;
    }
}
