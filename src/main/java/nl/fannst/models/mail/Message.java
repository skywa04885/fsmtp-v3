package nl.fannst.models.mail;

import com.mongodb.BasicDBObject;
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

public class Message extends DatabaseModel {
    /****************************************************
     * Data Types
     ****************************************************/

    public enum Flag {
        SEEN(0, "Seen"),
        ANSWERED(1, "Answered"),
        FLAGGED(2, "Flagged"),
        DELETED(3, "Deleted"),
        DRAFT(4, "Draft"),
        RECENT(5, "Recent");

        private final int m_Mask;
        private final String m_Keyword;

        Flag(int bit, String keyword) {
            m_Mask = (1 << bit);
            m_Keyword = keyword;
        }

        public int getMask() {
            return m_Mask;
        }

        public String getKeyword() {
            return m_Keyword;
        }

        @Override
        public String toString() {
            return '\\' + m_Keyword;
        }
    }


    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final byte[] m_EncryptedBody;
    private final byte[] m_DecryptionKey;
    private final int m_RawSize;
    private final String m_Subject;
    private final ArrayList<Address> m_MailFrom;
    private final ArrayList<Address> m_RcptTo;
    private int m_Flags;

    private final int m_UID;
    private final UUID m_AccountUUID;
    private int m_Folder;

    private final long m_Date;

    public Message(int uid, UUID accountUUID, int folder, int rawSize, byte[] decryptionKey, byte[] encryptedBody, String subject,
                   ArrayList<Address> mailFrom, ArrayList<Address> rcptTo, int flags, long date) {
        m_UID = uid;
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

    /**
     * Creates the document version of the current message.
     *
     * @return the document version of message.
     */
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


        // Creates the full document.
        return new Document("_id", createCompoundIndex(m_AccountUUID, m_UID))
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
     * Getters / Setters
     ****************************************************/

    public int getFlags() {
        return m_Flags;
    }

    public void setFlags(int flags) {
        m_Flags = flags;
    }

    public void setFlagMask(int mask) {
        m_Flags |= mask;
    }

    public void clearFlagMask(int mask) {
        m_Flags &= ~mask;
    }

    public int getFolder() {
        return m_Folder;
    }

    public void setFolder(int folder) {
        m_Folder = folder;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    /**
     * Gets all the UIDs from the specified mailbox, and where the specified
     *  flag / flags are set.
     *
     * @param mailbox the mailbox.
     * @param flags the flags to check.
     * @return the list of UIDs.
     */
    public static ArrayList<Integer> getUIDsWhereFlagSet(int mailbox, int flags) {
        // Gets all the documents where the specified bit is set, and the mailbox equals the specified
        //  mailbox, this mostly is used for search / select in IMAP.
        FindIterable<Document> documents = DatabaseConnection
                .getInstance()
                .getMessageCollection()
                .find(Filters.and(Arrays.asList(
                        Filters.bitsAllSet("flags", flags),
                        Filters.eq("mailbox", mailbox)
                ))).projection(Projections.fields(Projections.include("_id")))
                .limit(1000);

        // Builds the result list of integers ( message uuid's )
        ArrayList<Integer> uIDs = new ArrayList<>();
        for (Document document : documents)
            uIDs.add(Objects.requireNonNull((Document) document.get("_id")).getInteger("uid"));

        return uIDs;
    }

    /**
     * Gets all the UIDs from the specified mailbox, and where the specified
     *  flag / flags are cleared.
     *
     * @param mailbox the mailbox.
     * @param flags the flags to check.
     * @return the list of UIDs.
     */
    public ArrayList<Integer> getUIDsWhereFlagClear(int mailbox, int flags) {
        // Gets all the documents where the specified bit is clear, and the mailbox equals the specified
        //  mailbox, this mostly is used for search / select in IMAP.
        FindIterable<Document> documents = DatabaseConnection
                .getInstance()
                .getMessageCollection()
                .find(Filters.and(Arrays.asList(
                        Filters.bitsAllClear("flags", flags),
                        Filters.eq("mailbox", mailbox)
                ))).projection(Projections.fields(Projections.include("_id")))
                .limit(1000);

        // Builds the result list of integers ( message uuid's )
        ArrayList<Integer> uIDs = new ArrayList<>();
        for (Document document : documents)
            uIDs.add(Objects.requireNonNull((Document) document.get("_id")).getInteger("uid"));

        return uIDs;
    }

    /**
     * Gets the message body of the specified document.
     *
     * @param uid the unique identifier.
     * @return the body.
     */
    public static Pair<byte[], byte[]> getMessageBody(UUID accountUUID, int uid) {
        // Gets the decryption key and body with the specified UID, and account
        //  UUID.
        Document document = DatabaseConnection
                .getInstance()
                .getMessageCollection()
                .find(Filters.eq("_id", createCompoundIndex(accountUUID, uid)))
                .projection(Projections.fields(Arrays.asList(
                        Projections.include("key"),
                        Projections.include("body")
                )))
                .first();

        // If no document found, return null.
        if (document == null)
            return null;

        // Return the binary data.
        return new Pair<>(document.get("key", Binary.class).getData(),
                document.get("body", Binary.class).getData());
    }

    /**
     * Gets a set of UIDs of messages, and their size. This is used by POP3.
     *
     * @param accountUUID the UUID of the account.
     * @return the ArrayList of UIDs and sizes.
     */
    public static ArrayList<Pair<Integer, Integer>> getPOP3BasicInfo(UUID accountUUID) {
        byte[] binaryAccountUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(accountUUID.getMostSignificantBits())
                .putLong(accountUUID.getLeastSignificantBits())
                .array();

        // Gets all the documents from the specified account uuid.
        FindIterable<Document> documents = DatabaseConnection
                .getInstance()
                .getMessageCollection()
                .find(Filters.eq("_id.account_uuid", new Binary(BsonBinarySubType.UUID_STANDARD, binaryAccountUUID)))
                .projection(Projections.fields(Arrays.asList(
                        Projections.include("_id"),
                        Projections.include("raw_size")
                )))
                .limit(120);

        // Loops over all the found documents, and puts the data into the result
        //  array so we can read it later.
        ArrayList<Pair<Integer, Integer>> result = new ArrayList<>();
        for (Document document : documents) {
            result.add(new Pair<>(Objects.requireNonNull((Document) document.get("_id")).getInteger("uid"),
                    document.getInteger("raw_size")));
        }

        return result;
    }

    /**
     * Encrypts the specified body.
     *
     * @param raw the raw body.
     * @param rsaPublicKey the public key to use.
     * @return the encrypted body.
     */
    public static Pair<byte[], byte[]> encryptBody(String raw, PublicKey rsaPublicKey) {
        SecretKey secretKey = AES.generateKey();
        assert secretKey != null;

        byte[] encryptedBody = AES.easyEncrypt(raw.getBytes(), secretKey);

        byte[] rsaEncryptedAESKeyBytes = RSA.encrypt(secretKey.getEncoded(), rsaPublicKey);
        assert rsaEncryptedAESKeyBytes != null;

        return new Pair<>(encryptedBody, rsaEncryptedAESKeyBytes);
    }

    /**
     * Decrypts an message body with specified body, and key.
     *
     * @param body the body to decrypt.
     * @param key the decryption key.
     * @param privateKey the RSA private key.
     * @return the decrypted body.
     */
    public static byte[] decryptBody(byte[] body, byte[] key, PrivateKey privateKey) {
        byte[] decryptedKey = RSA.decrypt(key, privateKey);
        assert decryptedKey != null;

        SecretKey secretKey = new SecretKeySpec(decryptedKey, "AES");
        return AES.easyDecrypt(body, secretKey);
    }

    /**
     * Deletes all documents where the UID is in the list.
     *
     * @param accountUUID the UUID of the account.
     * @param uIDs the UIDs to delete.
     */
    public static void deleteMany(UUID accountUUID, ArrayList<Integer> uIDs) {
        ArrayList<BasicDBObject> deletable = new ArrayList<>();

        for (Integer uid : uIDs)
            deletable.add(createCompoundIndex(accountUUID, uid));

        DatabaseConnection
                .getInstance()
                .getMessageCollection()
                .deleteMany(Filters.in("_id", deletable));
    }

    /**
     * Creates an compound index for an message.
     *
     * @param accountUUID the account uuid.
     * @param uid the uid of the message.
     * @return the compound index.
     */
    public static BasicDBObject createCompoundIndex(UUID accountUUID, int uid) {
        byte[] binaryAccountUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(accountUUID.getMostSignificantBits())
                .putLong(accountUUID.getLeastSignificantBits())
                .array();

        BasicDBObject object = new BasicDBObject();
        object.append("account_uuid", new Binary(BsonBinarySubType.UUID_STANDARD, binaryAccountUUID));
        object.append("uid", uid);

        return object;
    }
}
