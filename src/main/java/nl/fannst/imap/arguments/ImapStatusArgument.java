package nl.fannst.imap.arguments;

import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.datatypes.ImapList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ImapStatusArgument extends ImapCommandArgument {
    /****************************************************
     * Data Types
     ****************************************************/

    public enum Item {
        MESSAGES("MESSAGES"),
        RECENT("RECENT"),
        UID_NEXT("UIDNEXT"),
        UID_VALIDITY("UIDVALIDITY"),
        UNSEEN("UNSEEN");

        private final String m_Keyword;

        Item(String keyword) {
            m_Keyword = keyword;
        }

        public String getKeyword() {
            return m_Keyword;
        }

        public static Item fromString(String a) {
            for (Item item : Item.values()) {
                if (item.getKeyword().equalsIgnoreCase(a))
                    return item;
            }

            return null;
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final String m_Mailbox;
    private final ArrayList<Item> m_RequestedItems;

    public ImapStatusArgument(String mailbox, ArrayList<Item> requestedItems) {
        m_Mailbox = mailbox;
        m_RequestedItems = requestedItems;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    private static final Pattern MAILBOX_PATTERN = Pattern.compile("^([A-Za-z0-9-_\\[\\]/]+)$");
    public static ImapStatusArgument parse(String raw) throws ImapCommand.SyntaxException {
        int pos = raw.indexOf(' ');
        if (pos == -1)
            throw new ImapCommand.SyntaxException("not enough arguments");

        String mailbox = raw.substring(0, pos);
        if (!MAILBOX_PATTERN.matcher(mailbox).matches())
            throw new ImapCommand.SyntaxException("invalid mailbox name");

        ArrayList<Item> items = new ArrayList<>();
        List<String> rawItems = ImapList.parse(raw.substring(pos + 1)).getList();

        for (String rawItem : rawItems) {
            Item item;
            if ((item = Item.fromString(rawItem)) == null)
                throw new ImapCommand.SyntaxException("invalid requested item");

            items.add(item);
        }


        return new ImapStatusArgument(mailbox, items);
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public String getMailbox() {
        return m_Mailbox;
    }

    public ArrayList<Item> getRequestedItems() {
        return m_RequestedItems;
    }
}
