package nl.fannst.models.accounts;

import com.mongodb.client.model.Filters;
import nl.fannst.DatabaseConnection;
import org.bson.Document;

import java.util.Locale;

public class Domain {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final String m_Domain;
    private final boolean m_Public;

    /**
     * Creates new domain instance
     * @param domain the domain
     * @param _public available for use without permission
     */
    public Domain(String domain, boolean _public) {
        m_Domain = domain;
        m_Public = _public;
    }

    /****************************************************
     * Database Operations
     ****************************************************/

    /**
     * Gets an domain from the database
     * @param domain the domain
     * @return the database domain
     */
    public static Domain get(String domain) {
        Document document = DatabaseConnection
                .getInstance()
                .getDomainCollection()
                .find(Filters.eq("_id", domain.toLowerCase(Locale.ROOT)))
                .first();

        if (document == null) {
            return null;
        }

        return fromDocument(document);
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    public static Domain fromDocument(Document document) {
        return new Domain(document.getString("_id"),
                document.getBoolean("public"));
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public String getDomain() {
        return m_Domain;
    }

    public boolean getPublic() {
        return m_Public;
    }
}
