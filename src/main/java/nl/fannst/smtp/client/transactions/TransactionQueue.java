package nl.fannst.smtp.client.transactions;

import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.smtp.SmtpReply;

import java.io.IOException;
import java.util.LinkedList;

public class TransactionQueue {
    /****************************************************
     * Flags
     ****************************************************/

    public static final byte PIPELINING = (1);

    /****************************************************
     * Data Types
     ****************************************************/

    public static enum OnReplyRetVal {
        Success,
        Failure,
        Last
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final LinkedList<Transaction> m_Transactions;
    private final LinkedList<TransactionError> m_Errors;
    private int m_TransactionExecutionIndex;
    private int m_TransactionCompletedIndex;

    private byte m_Flags;

    public TransactionQueue() {
        m_Transactions = new LinkedList<Transaction>();
        m_Errors = new LinkedList<TransactionError>();
        m_TransactionExecutionIndex = 0;
        m_TransactionCompletedIndex = 0;

        m_Flags = 0;
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Executes the transaction queue, either in normal mode or pipelining.
     * @param client the client to execute to
     * @throws IOException the possible IO Exception
     */
    public void execute(PlainNIOClientArgument client) throws IOException {
        executeTransaction(client);
    }

    private void executeTransaction(PlainNIOClientArgument client) throws IOException {
        Transaction transaction = m_Transactions.get(m_TransactionExecutionIndex++);
        System.out.println("Executing transaction: " + transaction.getClass().getName());
        transaction.execute(this, client);
    }

    /**
     * Performs the server-response part of an transaction, and returns
     *  if all transactions are finished.
     * @param client the client to execute to
     * @param reply the reply we've received
     * @return everything done ?
     * @throws Transaction.TransactionException possible error from transaction
     */
    public OnReplyRetVal onReply(PlainNIOClientArgument client, SmtpReply reply) throws Transaction.TransactionException {
        Transaction transaction = m_Transactions.get(m_TransactionCompletedIndex++);
        System.out.println("Reply for: " + transaction.getClass().getName());
        if (transaction.onReply(this, client, reply)) {
            return OnReplyRetVal.Failure;
        }

        return m_TransactionCompletedIndex >= m_Transactions.size() ? OnReplyRetVal.Last : OnReplyRetVal.Success;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public void addTransaction(Transaction transaction) {
        assert transaction != null : "Transaction may not be null.";
        m_Transactions.add(transaction);
    }

    public void addTransaction(int i, Transaction transaction) {
        assert transaction != null : "Transaction may not be null.";
        m_Transactions.add(i, transaction);
    }

    public boolean isFlagSet(byte flag) {
        return (m_Flags & flag) != 0;
    }

    public void setFlag(byte flag) {
        m_Flags |= flag;
    }

    public void clearFlag(byte flag) {
        m_Flags &= ~flag;
    }

    public void setTransactionExecutedIndex(int n) {
        m_TransactionExecutionIndex = n;
    }

    public LinkedList<TransactionError> getTransactionErrors() {
        return m_Errors;
    }

    public void addTransactionError(TransactionError error) {
        m_Errors.add(error);
    }
}
