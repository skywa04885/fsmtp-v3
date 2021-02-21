package nl.fannst.models.mail.mailbox_v2;

import org.bson.Document;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Mailbox {
    private static final String NAME_FIELD = "n";
    private static final String CHILDREN_FIELD = "c";
    private static final String META_FIELD = "m";
    private static final String ID_FIELD = "id";

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

    public Document toDocument(boolean head) {
        Document document = new Document();

        if (head)
            document.append(NAME_FIELD, m_Name);

        document.append(META_FIELD, m_Meta.toDocument());
        document.append(ID_FIELD, m_ID);

        if (m_Children != null) {
            Document children = new Document();
            m_Children.forEach(c -> {
                children.append(c.getName(), c.toDocument(false));
            });
            document.append(CHILDREN_FIELD, children);
        }

        return document;
    }

    public String getName() {
        return m_Name;
    }
}
