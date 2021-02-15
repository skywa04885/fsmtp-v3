package nl.fannst.smtp.client;

import nl.fannst.Logger;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.client.transactions.TransactionQueue;

public class SmtpClientSession {
    /****************************************************
     * Data Types
     ****************************************************/

    public static class Capabilities {

        /****************************************************
         * Flags
         ****************************************************/

        public static final byte s_EsmtpCapabilityFlag = (1);
        public static final byte s_PipeliningCapabilityFlag = (1 << 1);
        public static final byte s_BinaryMimeCapabilityFlag = (1 << 2);
        public static final byte s_ChunkingCapabilityFlag = (1 << 3);
        public static final byte s_EnhancedStatusCodesFlag = (1 << 4);
        public static final byte s_8BitMimeFlag = (1 << 5);
        public static final byte s_SmtpUTF8Flag = (1 << 6);

        /****************************************************
         * Classy Stuff
         ****************************************************/

        private int m_CapabilityFlags;
        private int m_MaxSize;

        public Capabilities(int flags, int maxSize) {
            m_CapabilityFlags = flags;
            m_MaxSize = maxSize;
        }

        /****************************************************
         * Instance Methods
         ****************************************************/

        public void log(Logger logger) {
            logger.log("(Capabilities): {");
            logger.log("\tMaxSize: " + m_MaxSize);
            logger.log("\tRawFlags: " + m_CapabilityFlags);
            logger.log("\tFlags: " + m_CapabilityFlags);

            if (getCapabilityFlag(s_BinaryMimeCapabilityFlag)) logger.log("\t\t- Binary Mime");
            if (getCapabilityFlag(s_EsmtpCapabilityFlag)) logger.log("\t\t- ESMTP");
            if (getCapabilityFlag(s_PipeliningCapabilityFlag)) logger.log("\t\t- PIPELINING");
            if (getCapabilityFlag(s_ChunkingCapabilityFlag)) logger.log("\t\t- Chunking");
            if (getCapabilityFlag(s_EnhancedStatusCodesFlag)) logger.log("\t\t- Enhanced Status Codes");
            if (getCapabilityFlag(s_8BitMimeFlag)) logger.log("\t\t- 8BBIT MIME");
            if (getCapabilityFlag(s_SmtpUTF8Flag)) logger.log("\t\t- SMTP UTF8");

            logger.log("}");
        }

        /****************************************************
         * Getters / Setters
         ****************************************************/

        public int getCapabilityFlags() {
            return m_CapabilityFlags;
        }

        public void setCapabilityFlag(int flag) {
            m_CapabilityFlags |= flag;
        }

        public boolean getCapabilityFlag(int flag) {
            return (m_CapabilityFlags & flag) != 0;
        }

        public void setMaxSize(int s) {
            m_MaxSize = s;
        }

        public int getMaxSize() {
            return m_MaxSize;
        }

        public boolean usesESMTP() {
            return getCapabilityFlag(s_EsmtpCapabilityFlag);
        }

        public boolean usesPipelining() {
            return getCapabilityFlag(s_PipeliningCapabilityFlag);
        }

        public boolean usesChunking() {
            return getCapabilityFlag(s_ChunkingCapabilityFlag);
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final Capabilities m_Capabilities;
    private final TransactionQueue m_TransactionQueue;
    private final SmtpClientTask m_Task;
    private SmtpReply m_Reply;

    public SmtpClientSession(SmtpClientTask task) {
        m_Capabilities = new Capabilities(0, 0);
        m_TransactionQueue = new TransactionQueue();
        m_Task = task;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public Capabilities getCapabilities() {
        return m_Capabilities;
    }

    public TransactionQueue getTransactionQueue() {
        return m_TransactionQueue;
    }

    public void setReply(SmtpReply reply) {
        m_Reply = reply;
    }

    public SmtpReply getReply() {
        return m_Reply;
    }

    public SmtpClientTask getTask() {
        return m_Task;
    }
}
