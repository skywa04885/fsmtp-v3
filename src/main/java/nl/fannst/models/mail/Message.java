package nl.fannst.models.mail;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import nl.fannst.DatabaseConnection;
import nl.fannst.datatypes.Pair;
import nl.fannst.encryption.AES;
import nl.fannst.encryption.RSA;
import nl.fannst.mime.Address;
import nl.fannst.models.DatabaseModel;
import org.bson.BsonBinarySubType;
import org.bson.BsonDateTime;
import org.bson.Document;
import org.bson.types.Binary;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.Data;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

public class Message extends DatabaseModel {
    /****************************************************
     * Data Types
     ****************************************************/

    public static enum DefaultFolder {
        Inbox(0),
        Spam(1),
        Sent(2),
        Archive(3),
        Drafts(4),
        Trash(5);

        private final int m_Code;

        DefaultFolder(int code) {
            m_Code = code;
        }

        public int getCode() {
            return m_Code;
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private byte[] m_EncryptedBody;
    private byte[] m_DecryptionKey;
    private int m_RawSize;
    private String m_Subject;
    private ArrayList<Address> m_MailFrom;
    private ArrayList<Address> m_RcptTo;
    private byte m_Flags;

    private UUID m_UUID;
    private UUID m_AccountUUID;
    private int m_Folder;

    private long m_Date;

    public Message(UUID uuid, UUID accountUUID, int folder, int rawSize, byte[] decryptionKey, byte[] encryptedBody, String subject,
                   ArrayList<Address> mailFrom, ArrayList<Address> rcptTo, byte flags, long date) {
        m_UUID = uuid;
        m_AccountUUID = accountUUID;
        m_RawSize = rawSize;
        m_Folder = folder;
        m_DecryptionKey = decryptionKey;
        m_EncryptedBody = encryptedBody;
        m_Subject = subject;
        m_MailFrom = mailFrom;
        m_RcptTo = rcptTo;
        m_Flags = flags;
        m_Date = date;
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    @Override
    public Document toDocument() {
        // Creates the document array version of the TO field value.
        ArrayList<Document> rcptTo = new ArrayList<Document>();
        m_RcptTo.forEach(address -> {
            rcptTo.add(new Document("name", address.getName())
                    .append("address", address.getAddress()));
        });

        // Creates the document version of the FROM field value.
        ArrayList<Document> mailFrom = new ArrayList<Document>();
        m_MailFrom.forEach(address -> {
            mailFrom.add(new Document("name", address.getName())
                    .append("address", address.getAddress()));
        });

        byte[] binaryUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(m_UUID.getMostSignificantBits())
                .putLong(m_UUID.getLeastSignificantBits())
                .array();

        byte[] binaryAccountUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(m_AccountUUID.getMostSignificantBits())
                .putLong(m_AccountUUID.getLeastSignificantBits())
                .array();

        // Creates the full document.
        return new Document("_id", new Binary(BsonBinarySubType.UUID_STANDARD, binaryUUID))
                .append("account_id", new Binary(BsonBinarySubType.UUID_STANDARD, binaryAccountUUID))
                .append("folder", m_Folder)
                .append("flags", m_Flags)
                .append("key", m_DecryptionKey)
                .append("body", m_EncryptedBody)
                .append("subject", m_Subject)
                .append("to", rcptTo)
                .append("from", mailFrom)
                .append("raw_size", m_RawSize)
                .append("date", new BsonDateTime(m_Date));
    }

    /**
     * Saves the current instance to the database
     */
    @Override
    public void save() {
        DatabaseConnection
                .getInstance()
                .getMessageCollection()
                .insertOne(toDocument());
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    public static Pair<byte[], byte[]> getMessageBody(UUID uuid) {
        byte[] binaryMessageUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();

        Document document = DatabaseConnection
                .getInstance()
                .getMessageCollection()
                .find(Filters.eq("_id", new Binary(BsonBinarySubType.UUID_STANDARD, binaryMessageUUID)))
                .projection(Projections.fields(Arrays.asList(
                        Projections.include("key"),
                        Projections.include("body")
                )))
                .first();

        if (document == null) {
            return null;
        }

        return new Pair<byte[], byte[]>(document.get("key", Binary.class).getData(), document.get("body", Binary.class).getData());
    }

    public static ArrayList<Pair<UUID, Integer>> getPOP3BasicInfo(UUID accountUUID) {
        byte[] binaryAccountUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(accountUUID.getMostSignificantBits())
                .putLong(accountUUID.getLeastSignificantBits())
                .array();

        FindIterable<Document> documents = DatabaseConnection
                .getInstance()
                .getMessageCollection()
                .find(Filters.eq("account_id", new Binary(BsonBinarySubType.UUID_STANDARD, binaryAccountUUID)))
                .projection(Projections.fields(Arrays.asList(
                        Projections.include("_id"),
                        Projections.include("raw_size")
                )))
                .limit(120);

        ArrayList<Pair<UUID, Integer>> result = new ArrayList<Pair<UUID, Integer>>();

        Iterator<Document> iterator = documents.iterator();
        while (iterator.hasNext()) {
            Document document = iterator.next();

            result.add(new Pair<UUID, Integer>((UUID) document.get("_id"), document.getInteger("raw_size")));
        }

        return result;
    }

    /**
     * Encrypts the specified body
     * @param raw the raw body
     * @param rsaPublicKey the public key to use
     * @return the encrypted body
     */
    public static Pair<byte[], byte[]> encryptBody(String raw, PublicKey rsaPublicKey) {
        SecretKey secretKey = AES.generateKey();
        assert secretKey != null;

        byte[] encryptedBody = AES.easyEncrypt(raw.getBytes(), secretKey);

        byte[] rsaEncryptedAESKeyBytes = RSA.encrypt(secretKey.getEncoded(), rsaPublicKey);
        assert rsaEncryptedAESKeyBytes != null;

        return new Pair<byte[], byte[]>(encryptedBody, rsaEncryptedAESKeyBytes);
    }

    public static byte[] decryptBody(byte[] body, byte[] key, PrivateKey privateKey) {
        byte[] decryptedKey = RSA.decrypt(key, privateKey);
        assert decryptedKey != null;

        SecretKey secretKey = new SecretKeySpec(decryptedKey, "AES");
        return AES.easyDecrypt(body, secretKey);
    }

    public static void deleteMany(ArrayList<UUID> uuids) {
        ArrayList<Binary> binaryUUIDs = new ArrayList<Binary>();
        binaryUUIDs.ensureCapacity(uuids.size());

        for (UUID uuid : uuids) {
            byte[] binaryUUID = ByteBuffer.wrap(new byte[16])
                    .order(ByteOrder.BIG_ENDIAN)
                    .putLong(uuid.getMostSignificantBits())
                    .putLong(uuid.getLeastSignificantBits())
                    .array();

            binaryUUIDs.add(new Binary(BsonBinarySubType.UUID_STANDARD, binaryUUID));
        }

        DatabaseConnection
                .getInstance()
                .getMessageCollection()
                .deleteMany(Filters.in("_id", binaryUUIDs));
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/
}
