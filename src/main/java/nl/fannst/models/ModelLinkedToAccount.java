package nl.fannst.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class ModelLinkedToAccount {
    protected static final String ACCOUNT_UUID_FIELD = "_id";

    protected UUID m_AccountUUID;

    protected ModelLinkedToAccount(UUID accountUUID) {
        m_AccountUUID = accountUUID;
    }

    public UUID getAccountUUID() {
        return m_AccountUUID;
    }

    public void setAccountUUID(UUID accountUUID) {
        m_AccountUUID = accountUUID;
    }

    public byte[] getBinaryAccountUUID() {
        return ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(m_AccountUUID.getMostSignificantBits())
                .putLong(m_AccountUUID.getLeastSignificantBits())
                .array();
    }
}
