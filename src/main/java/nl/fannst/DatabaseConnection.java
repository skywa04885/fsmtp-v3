package nl.fannst;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DatabaseConnection {
    /****************************************************
     * Singleton Stuff
     ****************************************************/

    private static DatabaseConnection INSTANCE;

    /**
     * Gets the current singleton instance
     * @return the instance
     */
    public static DatabaseConnection getInstance() {
        assert (INSTANCE != null);
        return INSTANCE;
    }

    /**
     * Creates the local instance, and connects to the database
     * @param uri the uri
     * @param database the database
     */
    public static void connect(String uri, String database) {
        assert(INSTANCE == null);
        INSTANCE = new DatabaseConnection(uri, database);
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final MongoCollection<Document> m_AccountsCollection;
    private final MongoCollection<Document> m_MailingListsCollection;
    private final MongoCollection<Document> m_DomainCollection;
    private final MongoCollection<Document> m_MessageCollection;

    /**
     * Default constructor for database connection
     * @param uri the uri
     * @param database the database
     */
    private DatabaseConnection(String uri, String database) {
        MongoClient mongoClient = new MongoClient(new MongoClientURI(uri));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(database);

        m_AccountsCollection = mongoDatabase.getCollection("accounts");
        m_MailingListsCollection = mongoDatabase.getCollection("mailing_lists");
        m_DomainCollection = mongoDatabase.getCollection("domains");
        m_MessageCollection = mongoDatabase.getCollection("messages");
    }

    /****************************************************
     * Getters
     ****************************************************/

    public MongoCollection<Document> getAccountsCollection() {
        return m_AccountsCollection;
    }

    public MongoCollection<Document> getMailingListsCollection() {
        return m_MailingListsCollection;
    }

    public MongoCollection<Document> getDomainCollection() {
        return m_DomainCollection;
    }

    public MongoCollection<Document> getMessageCollection() {
        return m_MessageCollection;
    }
}
