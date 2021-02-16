package nl.fannst.smtp.client.transactions.smtp;

import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.client.transactions.Transaction;
import nl.fannst.smtp.client.transactions.TransactionQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DataTransaction extends Transaction {
    private final ByteBuffer m_Body;

    public DataTransaction(ByteBuffer body) {
        m_Body = body;
    }

    @Override
    public void execute(TransactionQueue queue, PlainNIOClientArgument client) throws IOException {
        client.write(m_Body);
        client.write(ByteBuffer.wrap("\r\n.\r\n".getBytes(StandardCharsets.US_ASCII)));
    }

    @Override
    public boolean onReply(TransactionQueue queue, PlainNIOClientArgument client, SmtpReply reply) throws TransactionException {
        return false;
    }
}
