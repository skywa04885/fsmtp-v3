package nl.fannst.smtp.client.transactions.chunking;

import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.client.transactions.CommandTransaction;
import nl.fannst.smtp.client.transactions.TransactionError;
import nl.fannst.smtp.client.transactions.TransactionQueue;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BdatTransaction extends CommandTransaction {
    private final ByteBuffer m_Chunk;

    public BdatTransaction(ByteBuffer chunk, boolean last) {
        super(new SmtpCommand(SmtpCommand.Type.BDAT, new String[] {
                Integer.toString(chunk.remaining()),
                last ? "LAST" : ""
        }));

        m_Chunk = chunk;
    }

    @Override
    public void execute(TransactionQueue queue, PlainNIOClientArgument client) throws IOException {
        super.execute(queue, client);
        client.write(m_Chunk);
    }

    @Override
    public boolean onReply(TransactionQueue queue, PlainNIOClientArgument client, SmtpReply reply) throws TransactionException {
        if (reply.getCode() == 250) {
            return false;
        }

        queue.addTransactionError(new TransactionError(getCommand().toString(), reply.toString(false)));
        return true;
    }
}
