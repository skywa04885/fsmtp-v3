package nl.fannst.smtp.client.transactions;

import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.smtp.SmtpReply;

import java.io.IOException;

public class Transaction {
    public static class TransactionException extends Exception {
        private final int m_Code;

        public TransactionException(int code, String message) {
            super(message);
            m_Code = code;
        }
    }

    public Transaction() {}

    public void execute(TransactionQueue queue, PlainNIOClientArgument client) throws IOException {
    }

    public void onReply(TransactionQueue queue, PlainNIOClientArgument client, SmtpReply reply) throws TransactionException {
        assert false : "Transaction handler MUST be overwritten.";
    }
}
