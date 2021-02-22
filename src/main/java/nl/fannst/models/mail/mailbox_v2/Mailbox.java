package nl.fannst.models.mail.mailbox_v2;

import org.bson.Document;

import javax.print.Doc;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Mailbox {
    /****************************************************
     * MongoDB Fields
     ****************************************************/

    private static final String NAME_FIELD = "n";
    private static final String CHILDREN_FIELD = "c";
    private static final String META_FIELD = "m";
    private static final String ID_FIELD = "id";

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private String m_Name;
    private int m_ID;
    private ArrayList<Mailbox> m_Children;
    private MailboxMeta m_Meta;

    public Mailbox(String name, int id, ArrayList<Mailbox> children, MailboxMeta meta) {
        m_ID = id;
        m_Name = name;
        m_Children = children;
        m_Meta = meta;
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

    public String getName() {
        return m_Name;
    }

    public ArrayList<Mailbox> getChildren() {
        return m_Children;
    }

    public MailboxMeta getMeta() {
        return m_Meta;
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
