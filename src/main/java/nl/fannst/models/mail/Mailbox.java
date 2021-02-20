package nl.fannst.models.mail;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.DBCollectionCountOptions;
import com.mongodb.client.model.Filters;
import nl.fannst.DatabaseConnection;
import nl.fannst.models.DatabaseModel;
import org.bson.BsonBinarySubType;
import org.bson.Document;
import org.bson.types.Binary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class Mailbox extends DatabaseModel {
    /****************************************************
     * Data Types
     ****************************************************/

    public enum Flag {
        Incoming(0, "Incoming"),
        Outgoing(1, "Outgoing"),
        Suspicious(2, "Suspicious"),
        System(3, "System"),
        Solid(4, "Solid");

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
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final UUID m_AccountUUID;
    private final int m_MailboxID;

    private String m_MailboxName;
    private int m_MessageCount;
    private int m_Flags;

    /**
     * Creates an new mailbox instance.
     *
     * @param accountUUID the uuid of account.
     * @param mailboxID the numeric id of mailbox.
     * @param mailboxName the name of mailbox.
     * @param messageCount the number of messages in mailbox.
     * @param flags the flags of mailbox.
     */
    public Mailbox(UUID accountUUID, int mailboxID, String mailboxName, int messageCount, int flags) {
        m_AccountUUID = accountUUID;
        m_MailboxID = mailboxID;
        m_MailboxName = mailboxName;
        m_MessageCount = messageCount;
        m_Flags = flags;
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /****************************************************
     * Override Methods
     ****************************************************/

    /**
     * Saves the current Mailbox instance into mongodb.
     */
    @Override
    public void save() {
        DatabaseConnection
                .getInstance()
                .getMailboxesCollection()
                .insertOne(toDocument());
    }

    /**
     * Creates the document version of current mailbox.
     *
     * @return the document version.
     */
    @Override
    public Document toDocument() {
        Document document = new Document("_id", createCompoundIndex(m_AccountUUID, m_MailboxID));

        document.append("message_count", m_MessageCount);
        document.append("name", m_MailboxName);
        document.append("flags", m_Flags);

        return document;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public UUID getAccountUUID() {
        return m_AccountUUID;
    }

    public int getMailboxID() {
        return m_MailboxID;
    }

    public String getMailboxName() {
        return m_MailboxName;
    }

    public int getMessageCount() {
        return m_MessageCount;
    }

    public int getFlags() {
        return m_Flags;
    }

    public void setMailboxName(String name) {
        m_MailboxName = name;
    }

    public void setMessageCount(int cnt) {
        m_MessageCount = cnt;
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

    /****************************************************
     * Static Methods
     ****************************************************/

    /**
     * Gets an mailbox class from document.
     *
     * @param document the raw document.
     * @return the mailbox class.
     */
    public static Mailbox fromDocument(Document document) {
        Document _id = (Document) document.get("_id");

        return new Mailbox((UUID) _id.get("account_uuid"),
                _id.getInteger("id"),
                document.getString("name"),
                document.getInteger("message_count"),
                document.getInteger("flags"));
    }


    /**
     * Increments the message count in the specified mailbox.
     *
     * @param accountUUID the UUID of the account ( owner of mailbox ).
     * @param id the id of the mailbox.
     */
    public static void incMessageCount(UUID accountUUID, int id) {
        byte[] binaryAccountUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(accountUUID.getMostSignificantBits())
                .putLong(accountUUID.getLeastSignificantBits())
                .array();

        // Creates the increment operation.
        BasicDBObject update = new BasicDBObject("$inc", new BasicDBObject("message_count", 1));

        // Executes the operation.
        DatabaseConnection
                .getInstance()
                .getMailboxesCollection()
                .updateOne(Filters.and(
                        Filters.eq("_id.id", id),
                        Filters.eq("_id.account_uuid", new Binary(BsonBinarySubType.UUID_STANDARD, binaryAccountUUID))
                ), update);
    }

    /**
     * Gets all the mailboxes from the specified account.
     *
     * @param accountUUID the account uuid.
     * @return the list of found mailboxes.
     */
    public static ArrayList<Mailbox> getByAccount(UUID accountUUID) {
        // Gets the binary version of the specified UUID, this is required
        //  to correctly perform the query.
        byte[] binaryAccountUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(accountUUID.getMostSignificantBits())
                .putLong(accountUUID.getLeastSignificantBits())
                .array();

        // Gets all the matching documents from the database.
        FindIterable<Document> documents = DatabaseConnection
                .getInstance()
                .getMailboxesCollection()
                .find(Filters.eq("_id.account_uuid", new Binary(BsonBinarySubType.UUID_STANDARD, binaryAccountUUID)));

        // Creates the list of mailboxes from the found documents.
        ArrayList<Mailbox> mailboxes = new ArrayList<>();
        for (Document document : documents)
            mailboxes.add(Mailbox.fromDocument(document));

        return mailboxes;
    }

    /**
     * Gets an mailbox where the specified flag is set.
     *
     * @param accountUUID the account uuid.
     * @param flags the flags.
     * @return the found mailbox.
     */
    public static Mailbox getByFlag(UUID accountUUID, int flags) {
        // Gets the binary version of the specified UUID, this is required
        //  to correctly perform the query.
        byte[] binaryAccountUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(accountUUID.getMostSignificantBits())
                .putLong(accountUUID.getLeastSignificantBits())
                .array();

        // Gets the matching document where the specified flag is set.
        Document document = DatabaseConnection
                .getInstance()
                .getMailboxesCollection()
                .find(Filters.and(Arrays.asList(
                        Filters.eq("_id.account_uuid", new Binary(BsonBinarySubType.UUID_STANDARD, binaryAccountUUID)),
                        Filters.bitsAllSet("flags", flags)
                )))
                .first();

        // If null just return null, else return the class version.
        if (document == null) return null;
        else return fromDocument(document);
    }


    /**
     * Creates an compound index for an mailbox.
     *
     * @param accountUUID the account uuid.
     * @param id the id of the mailbox.
     * @return the compound index.
     */
    public static BasicDBObject createCompoundIndex(UUID accountUUID, int id) {
        byte[] binaryAccountUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(accountUUID.getMostSignificantBits())
                .putLong(accountUUID.getLeastSignificantBits())
                .array();

        BasicDBObject object = new BasicDBObject();
        object.append("account_uuid", new Binary(BsonBinarySubType.UUID_STANDARD, binaryAccountUUID));
        object.append("id", id);

        return object;
    }
}
