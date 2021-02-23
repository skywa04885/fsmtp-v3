package nl.fannst.models.mail.mailbox_v2;

import org.bson.Document;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Mailbox {
    /****************************************************
     * Static Variables
     ****************************************************/

    public static final int NAME_CHANGE_BIT = (1);
    public static final int CHILDREN_CHANGE_BIT = (1 << 1);
    public static final int META_CHANGE_BIT = (1 << 1);

    public static final String NAME_FIELD = "n";
    public static final String CHILDREN_FIELD = "c";
    public static final String META_FIELD = "m";
    public static final String ID_FIELD = "id";

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private String m_Name;
    private int m_ID;
    private ArrayList<Mailbox> m_Children;
    private MailboxMeta m_Meta;

    private int m_ChangeBits;

    public Mailbox(String name, int id, ArrayList<Mailbox> children, MailboxMeta meta) {
        m_ID = id;
        m_Name = name;
        m_Children = children;
        m_Meta = meta;
        m_ChangeBits = 0;
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    public Document toDocument() {
        Document document = new Document();

        document.append(NAME_FIELD, m_Name);
        document.append(META_FIELD, m_Meta.toDocument());
        document.append(ID_FIELD, m_ID);

        if (m_Children != null) {
            ArrayList<Document> children = new ArrayList<>();

            m_Children.forEach(c -> children.add(c.toDocument()));

            document.append(CHILDREN_FIELD, children);
        }

        return document;
    }

    public Document toUpdateDocument() {
        Document document = new Document();

        if ((m_ChangeBits & NAME_CHANGE_BIT) != 0)
            document.append(NAME_FIELD, m_Name);
        if ((m_ChangeBits & META_CHANGE_BIT) != 0)
            document.append(META_FIELD, m_Meta.toUpdateDocument());
        if ((m_ChangeBits & CHILDREN_CHANGE_BIT) != 0)
            document.append(CHILDREN_FIELD, m_Children.stream()
                .map(Mailbox::toUpdateDocument)
                .collect(Collectors.toCollection(ArrayList::new)));

        return document;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public void setMetaChangeBit() {
        m_ChangeBits |= META_CHANGE_BIT;
    }

    public int getChangeBits() {
        return m_ChangeBits;
    }

    public String getName() {
        return m_Name;
    }

    public ArrayList<Mailbox> getChildren() {
        return m_Children;
    }

    public MailboxMeta getMeta() {
        return m_Meta;
    }

    public int getID() {
        return m_ID;
    }

    public void setName(String name) {
        m_ChangeBits |= NAME_CHANGE_BIT;
        m_Name = name;
    }

    public void setChildren(ArrayList<Mailbox> children) {
        m_ChangeBits |= CHILDREN_CHANGE_BIT;
        m_Children = children;
    }

    public void setMeta(MailboxMeta meta) {
        m_ChangeBits |= META_CHANGE_BIT;
        m_Meta = meta;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    public static Mailbox fromDocument(Document document) {
        return new Mailbox(document.getString(NAME_FIELD),
                document.getInteger(ID_FIELD),
                document.getList(CHILDREN_FIELD, Document.class)
                        .stream()
                        .map(Mailbox::fromDocument)
                        .collect(Collectors.toCollection(ArrayList::new)),
                MailboxMeta.fromDocument((Document) document.get(META_FIELD)));
    }
}
