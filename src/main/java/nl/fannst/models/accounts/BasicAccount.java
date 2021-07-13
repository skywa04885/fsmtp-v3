package nl.fannst.models.accounts;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import nl.fannst.DatabaseConnection;
import nl.fannst.datatypes.Pair;
import nl.fannst.mime.Address;
import nl.fannst.models.BasicDocument;
import org.bson.BsonBinarySubType;
import org.bson.Document;
import org.bson.types.Binary;

import javax.xml.crypto.Data;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

public class BasicAccount extends BasicDocument {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final String m_Username;
    private final String m_Domain;
    private final String m_RSAPublic;
    private final String m_FullName;
    private final String m_Password;
    private final UUID m_UUID;
    private int m_NextUID;

    public BasicAccount(String username, String domain, String fullName, String password, String rsaPublic, UUID uuid, int nextUID) {
        m_Username = username;
        m_Domain = domain;
        m_FullName = fullName;
        m_Password = password;
        m_RSAPublic = rsaPublic;
        m_UUID = uuid;
        m_NextUID = nextUID;
    }

    /****************************************************
     * Database Operations
     ****************************************************/

    /**
     * Finds an basic account with username and domain
     * @param username the username
     * @param domain the domain
     * @return the basic account
     */
    public static BasicAccount find(String username, String domain) {
        Document document = DatabaseConnection.getInstance().getAccountsCollection().find(Filters.and(Arrays.asList(
                Filters.eq("username", username.toLowerCase(Locale.ROOT)),
                Filters.eq("domain", domain.toLowerCase(Locale.ROOT))
        ))).projection(Projections.fields(Arrays.asList(
                Projections.include("_id"),
                Projections.include("rsa_public"),
                Projections.include("full_name"),
                Projections.include("password"),
                Projections.include("next_uid")
        ))).first();

        if (document == null) {
            return null;
        }

        return new BasicAccount(username,
                domain,
                document.getString("full_name"),
                document.getString("password"),
                document.getString("rsa_public"),
                (UUID) document.get("_id"),
                document.getInteger("next_uid"));
    }


    public static String getPrivateKey(UUID uuid) {
        byte[] binaryUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();

        Document document = DatabaseConnection
                .getInstance()
                .getAccountsCollection()
                .find(Filters.eq("_id", new Binary(BsonBinarySubType.UUID_STANDARD, binaryUUID)))
                .projection(Projections.fields(Arrays.asList(
                        Projections.include("rsa_private")
                ))).first();

        if (document == null) {
            return null;
        }

        return document.getString("rsa_private");
    }

    /**
     * Finds an basic account with username only
     * @param username the username
     * @return the basic account
     */
    public static Pair<BasicAccount, Boolean> find(String username) {
        // Gets all the users with the specified username, domain does not matter
        FindIterable<Document> documents = DatabaseConnection
                .getInstance()
                .getAccountsCollection()
                .find(Filters.and(Filters.eq("username", username.toLowerCase(Locale.ROOT))))
                .projection(Projections.fields(Arrays.asList(
                        Projections.include("_id"),
                        Projections.include("rsa_public"),
                        Projections.include("full_name"),
                        Projections.include("domain"),
                        Projections.include("password")
                )));

        // Gets the cursor and checks if there is even somethign available
        MongoCursor<Document> cursor = documents.iterator();
        if (!cursor.hasNext()) {
            return new Pair<BasicAccount, Boolean>(null, false);
        }

        // Gets the first document found by the query
        Document document = cursor.next();
        BasicAccount basicAccount = new BasicAccount(username,
                document.getString("domain"),
                document.getString("full_name"),
                document.getString("password"),
                document.getString("rsa_public"),
                (UUID) document.get("_id"),
                document.getInteger("next_uid"));

        // Returns the new pair with the account, and the boolean indicating
        //  if there are more accounts left
        return new Pair<BasicAccount, Boolean>(
                basicAccount,
                cursor.hasNext()
        );
    }


    /****************************************************
     * Static Methods
     ****************************************************/

    /**
     * Gets the class version from MongoDB document
     * @param document the document
     * @return the class version
     */
    public static BasicAccount fromDocument(Document document) {
        return new BasicAccount(document.getString("username"),
                document.getString("domain"),
                document.getString("full_name"),
                document.getString("password"),
                document.getString("rsa_public"),
                (UUID) document.get("_id"),
                document.getInteger("next_uid"));
    }

    public static Integer getNextUid(UUID uuid) {
        byte[] binaryUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();

        Document document = DatabaseConnection
                .getInstance()
                .getAccountsCollection()
                .find(Filters.eq("_id", new Binary(BsonBinarySubType.UUID_STANDARD, binaryUUID)))
                .projection(Projections.fields(Arrays.asList(
                        Projections.include("next_uid")
                ))).first();

        if (document == null)
            return null;

        return document.getInteger("next_uid");
    }

    public static Integer getNextUidAndIncrement(UUID uuid) {
        byte[] binaryUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();

        BasicDBObject update = new BasicDBObject("$inc", new BasicDBObject("next_uid", 1));

        Document document = DatabaseConnection
                .getInstance()
                .getAccountsCollection()
                .findOneAndUpdate(Filters.eq("_id", new Binary(BsonBinarySubType.UUID_STANDARD, binaryUUID)), update);

        if (document == null)
            return null;

        return document.getInteger("next_uid");
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public String getUsername() {
        return m_Username;
    }

    public String getDomain() {
        return m_Domain;
    }

    public String getRSAPublic() {
        return m_RSAPublic;
    }

    public UUID getUUID() {
        return m_UUID;
    }

    public String getFulLName() {
        return m_FullName;
    }

    public Address getAddress() {
        return new Address(m_Username + '@' + m_Domain, m_FullName);
    }

    public String getPassword() {
        return m_Password;
    }
}
