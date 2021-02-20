package nl.fannst;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DatabaseConnection {
    private static final String DATABASE = "fannstv2";

    private static final String ACCOUNTS_COLLECTION = "accounts";
    private static final String MAILING_LISTS = "mailing_lists";
    private static final String DOMAINS = "domains";
    private static final String MESSAGES = "messages";
    private static final String MAILBOXES = "mailboxes";

    /****************************************************
     * Singleton Stuff
     ****************************************************/

    private static DatabaseConnection INSTANCE;

    /**
     * Gets the current singleton instance.
     *
     * @return the instance
     */
    public static DatabaseConnection getInstance() {
        assert (INSTANCE != null);
        return INSTANCE;
    }

    /**
     * Creates the local instance, and connects to the database.
     *
     * @param uri the uri
     */
    public static void createInstance(String uri) {
        assert(INSTANCE == null);
        INSTANCE = new DatabaseConnection(uri);
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final MongoCollection<Document> m_AccountsCollection;
    private final MongoCollection<Document> m_MailingListsCollection;
    private final MongoCollection<Document> m_DomainCollection;
    private final MongoCollection<Document> m_MessageCollection;
    private final MongoCollection<Document> m_MailboxesCollection;

    /**
     * Default constructor for database connection
     *
     * @param uri the URI to connect to.
     */
    private DatabaseConnection(String uri) {
        // Creates the mongo client, after which we get the database.
        MongoClient mongoClient = new MongoClient(new MongoClientURI(uri));
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DATABASE);

        // Gets all the collections we will use in the mail server.
        m_AccountsCollection = mongoDatabase.getCollection(ACCOUNTS_COLLECTION);
        m_MailingListsCollection = mongoDatabase.getCollection(MAILING_LISTS);
        m_DomainCollection = mongoDatabase.getCollection(DOMAINS);
        m_MessageCollection = mongoDatabase.getCollection(MESSAGES);
        m_MailboxesCollection = mongoDatabase.getCollection(MAILBOXES);
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

    public MongoCollection<Document> getMailboxesCollection() {
        return m_MailboxesCollection;
    }
}
