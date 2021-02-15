package nl.fannst.smtp.client.transactions.smtp;

import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.client.SmtpClientSession;
import nl.fannst.smtp.client.transactions.Transaction;
import nl.fannst.smtp.client.transactions.TransactionQueue;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;

public class GreetTransaction extends Transaction {
    public GreetTransaction() {
        super();
    }

    @Override
    public void execute(TransactionQueue queue, PlainNIOClientArgument client) throws IOException {}

    @Override
    public void onReply(TransactionQueue queue, PlainNIOClientArgument client, SmtpReply reply) throws TransactionException {
        SmtpClientSession session = (SmtpClientSession) client.getClientWrapper().attachment();

        try {
            if (reply.getMessage().toLowerCase(Locale.ROOT).contains("esmtp")) {
                session.getTransactionQueue().addTransaction(1, new EhloTransaction(InetAddress.getLocalHost().getHostName()));
            } else {
                session.getTransactionQueue().addTransaction(1, new HeloTransaction(InetAddress.getLocalHost().getHostName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
