package nl.fannst.smtp.client.transactions;

public class TransactionError {
    private final long m_Timestamp;
    private final String m_TransactionName;
    private final String m_Message;

    public TransactionError(String transactionName, String message) {
        m_Timestamp = System.currentTimeMillis();
        m_TransactionName = transactionName;
        m_Message = message;
    }

    public long getTimestamp() {
        return m_Timestamp;
    }

    public String getTransactionName() {
        return m_TransactionName;
    }

    public String getMessage() {
        return m_Message;
    }
}
