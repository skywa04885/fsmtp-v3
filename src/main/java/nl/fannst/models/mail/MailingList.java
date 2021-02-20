package nl.fannst.models.mail;

import com.mongodb.client.model.Filters;
import nl.fannst.DatabaseConnection;
import nl.fannst.mime.Address;
import nl.fannst.models.DatabaseModel;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class MailingList extends DatabaseModel {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final ArrayList<Address> m_Addresses;
    private final String m_Name;
    private final UUID m_UUID;

    /**
     * Creates new mailing list instance
     * @param addresses the addresses
     * @param name the name
     * @param uuid the uuid
     */
    public MailingList(ArrayList<Address> addresses, String name, UUID uuid) {
        m_Addresses = addresses;
        m_Name = name;
        m_UUID = uuid;
    }

    /**
     * Creates new mailing list instance with only addresses and name
     * @param addresses the addresses
     * @param name the name
     */
    public MailingList(ArrayList<Address> addresses, String name) {
        this(addresses, name, UUID.randomUUID());
    }

    /****************************************************
     * Database Operations
     ****************************************************/

    /**
     * Gets an mailing list by name
     * @param name the name of list
     * @return the possible list
     */
    public static MailingList get(String name) {
        // Attempts to get the mailing list from MongoDB
        Document document = DatabaseConnection
                .getInstance()
                .getMailingListsCollection()
                .find(Filters.eq("name", name.toLowerCase(Locale.ROOT)))
                .first();

        // If no list, just return null already
        if (document == null)
            return null;

        // Returns the class version of the document
        return fromDocument(document);
    }

    @Override
    public void save() {

    }

    @Override
    public Document toDocument() {
        return null;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    /**
     * Returns class version from mailing list
     * @param document the document
     * @return the class version
     */
    public static MailingList fromDocument(Document document) {
        ArrayList<Address> addresses = new ArrayList<Address>();

        document.getList("addresses", String.class).forEach(address -> {
            try {
                addresses.add(new Address(address));
            } catch (Address.InvalidAddressException e) {
                e.printStackTrace();
            }
        });

        return new MailingList(addresses,
                document.getString("name"),
                (UUID) document.get("_id"));
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public ArrayList<Address> getAddresses() {
        return m_Addresses;
    }

    public String getName() {
        return m_Name;
    }

    public UUID getUUID() {
        return m_UUID;
    }
}
