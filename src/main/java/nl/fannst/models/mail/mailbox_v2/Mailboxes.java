package nl.fannst.models.mail.mailbox_v2;

import com.mongodb.client.model.Filters;
import nl.fannst.DatabaseConnection;
import nl.fannst.datatypes.Pair;
import nl.fannst.models.ModelLinkedToAccount;
import org.bson.BsonBinarySubType;
import org.bson.Document;
import org.bson.types.Binary;

import javax.print.Doc;
import javax.xml.crypto.Data;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Mailboxes extends ModelLinkedToAccount {
    /****************************************************
     * Static Variables
     ****************************************************/

    private static final int HEADS_CHANGE_BIT = (1);
    private static final int NEXT_ID_CHANGE_BIT = (1 << 1);

    private static final String HEADS_FIELD = "h";
    private static final String NEXT_ID_FIELD = "n";

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private ArrayList<Mailbox> m_Heads;
    private int m_NextID;

    private int m_ChangeBits;

    public Mailboxes(UUID accountUUID, ArrayList<Mailbox> heads, int nextID) {
        super(accountUUID);

        m_Heads = heads;
        m_NextID = 0;
        m_ChangeBits = 0;
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Creates the document version of the class.
     *
     * @return the document version.
     */
    public Document toDocument() {
        Document document = new Document();

        document.append(ACCOUNT_UUID_FIELD, getBinaryAccountUUID());
        document.append(NEXT_ID_FIELD, m_NextID);

        if (m_Heads != null) {
            ArrayList<Document> heads = new ArrayList<>();

            m_Heads.forEach(head -> heads.add(head.toDocument()));

            document.append(HEADS_FIELD, heads);
        }

        return document;
    }

    /**
     * Deletes an mailbox from the tree.
     * @param dir the specific directory.
     * @return was there something deleted ?
     */
    public boolean deleteMailbox(String dir) {
        String[] segments = dir.split("/");

        ArrayList<Mailbox> a = m_Heads;

        int i = 0;
        for (String segment : segments) {
            Mailbox b = null;

            for (Mailbox aN : a) {
                if (aN.getName().equals(segment)) {
                    b = aN;
                }
            }

            if (b == null)
                return false;
            else if (i + 1 > segments.length) {
                a.remove(b);
                break;
            } else if (!b.getChildren().isEmpty())
                a = b.getChildren();

            ++i;
        }

        m_ChangeBits |= HEADS_CHANGE_BIT;
        return true;
    }

    /**
     * Inserts a new mailbox into the tree.
     *
     * @param dir the directory.
     * @param metas the meta data ( if new ).
     */
    public void insertMailbox(String dir, MailboxMeta ...metas) {
        m_ChangeBits |= HEADS_CHANGE_BIT;

        String[] segments = dir.split("/");

        ArrayList<Mailbox> a = m_Heads;
        int i = 0;
        for (String segment : segments) {
            // Attempt to get existing mailbox, match this with
            //  case sensitive names.
            Mailbox b = null;
            for (Mailbox aN : a) {
                if (aN.getName().equals(segment)) {
                    b = aN;
                    break;
                }
            }

            // If there was no mailbox found, create a new one with the possibly
            //  specified meta in the metas.
            if (b == null) {
                MailboxMeta meta = null;

                if (i < metas.length)
                    meta = metas[i];

                if (meta == null)
                    meta = new MailboxMeta();

                b = new Mailbox(segment, getAndIncrementNextID(), new ArrayList<>(), meta);
                a.add(b);
            }

            // Set a to new children, and increment index.
            a = b.getChildren();
            ++i;
        }
    }

    /**
     * Gets an mailbox from the current instance.
     *
     * @param dir the directory.
     * @return the found mailbox, or null ?
     */
    public Mailbox getInstanceMailbox(String dir) {
        String[] segments = dir.split("/");

        ArrayList<Mailbox> a = m_Heads;
        int i = 0;
        for (String segment : segments) {
            Mailbox b = null;
            for (Mailbox aN : a) {
                if (aN.getName().equals(segment)) {
                    b = aN;
                    break;
                }
            }

            if (b == null) return null;
            else if (i + 1 >= segments.length) return b;
            else a = b.getChildren();

            ++i;
        }

        return null;
    }

    /**
     * Gets all the mailboxes where the specified flag is set.
     *
     * @param a the list to search through.
     * @param flag the flag to check.
     * @param set check for set or clear ?
     * @return the matching mailboxes.
     */
    public static ArrayList<Mailbox> getBySystemFlag(ArrayList<Mailbox> a, MailboxMeta.SystemFlags flag, boolean set) {
        ArrayList<Mailbox> mailboxes = new ArrayList<>();

        // Loops over all elements of the structure, and asks all the meta
        //  objects if they have the specified flag set.
        for (Mailbox b : a) {
            if (set && b.getMeta().isSystemFlagSet(flag))
                mailboxes.add(b);
            else if (!set && b.getMeta().isSystemFlagClear(flag))
                mailboxes.add(b);

            if (!b.getChildren().isEmpty())
                mailboxes.addAll(getBySystemFlag(b.getChildren(), flag, set));
        }

        return mailboxes;
    }


    /**
     * Gets all the mailboxes where the specified flag is set.
     *
     * @param a the list to search through.
     * @param flag the flag to check.
     * @param set check for set or clear ?
     * @return the matching mailboxes.
     */
    public static ArrayList<Mailbox> getByImapFlag(ArrayList<Mailbox> a, MailboxMeta.ImapFlags flag, boolean set) {
        ArrayList<Mailbox> mailboxes = new ArrayList<>();

        // Loops over all elements of the structure, and asks all the meta
        //  objects if they have the specified flag set.
        for (Mailbox b : a) {
            if (set && b.getMeta().isImapFlagSet(flag))
                mailboxes.add(b);
            else if (!set && b.getMeta().isImapFlagClear(flag))
                mailboxes.add(b);

            if (!b.getChildren().isEmpty())
                mailboxes.addAll(getByImapFlag(b.getChildren(), flag, set));
        }

        return mailboxes;
    }

    /**
     * Gets all the mailboxes from tree where specified flag is set.
     *
     * @param flag the flag.
     * @param set check for set or clear ?
     * @return the matching mailboxes.
     */
    public ArrayList<Mailbox> getByImapFlag(MailboxMeta.ImapFlags flag, boolean set) {
        return getByImapFlag(m_Heads, flag, set);
   }

    /**
     * Gets all the mailboxes from tree where specified flag is set.
     *
     * @param flag the flag.
     * @param set check for set or clear ?
     * @return the matching mailboxes.
     */
    public ArrayList<Mailbox> getBySystemFlag(MailboxMeta.SystemFlags flag, boolean set) {
        return getBySystemFlag(m_Heads, flag, set);
    }

    /**
     * Tokenizes the specified wildcard.
     *
     * @param wildcard the wildcard.
     * @return the tokens.
     */
    private ArrayList<String> tokenizeWildcard(String wildcard) {
        ArrayList<String> tokens = new ArrayList<String>();

        int s = 0, e = 0;
        char[] chars = wildcard.toCharArray();
        for (char c : chars) {
            if (c == '*' || c == '%') {
                if (s != e) {
                    tokens.add(wildcard.substring(s, e));
                    s = e + 1;
                }

                tokens.add(Character.toString(c));
                if (s == 0)
                    ++s;
            }

            ++e;
        }

        if (s < e) {
            tokens.add(wildcard.substring(s, e));
        }

        return tokens;
    }

    /**
     * Matches an name with specified wildcard.
     *
     * @param wildcard the wildcard.
     * @param name the name.
     * @return matches ?
     */
    public boolean matchNameWildcard(String wildcard, String name) {
        ArrayList<String> tokens = tokenizeWildcard(wildcard);
        StringBuilder regexp = new StringBuilder("^(");

        // Loops over all the tokens, and builds the result regex string.
        for (String token : tokens) {
            if (token.equals("*") || token.equals("%")) {
                regexp.append("(([a-zA-Z0-9_])+?)");
                if (token.equals("%"))
                    break;
            } else regexp.append('(').append(token).append(')');
        }

        // Adds the closing pair.
        regexp.append(")$");

        // Compiles the generated regex, and matches it against the specified
        //  name.
        Pattern pattern = Pattern.compile(regexp.toString());
        return pattern.matcher(name).matches();
    }

    /**
     * Performs some mailbox wildcard matching.
     *
     * @param hierarchy the previous hierarchy.
     * @param a the current list of mailboxes.
     * @param wildcard the spliced wildcard.
     * @param from the from index.
     * @return the list of matches.
     */
    public ArrayList<Pair<String, Mailbox>> matchWildcard(String hierarchy, ArrayList<Mailbox> a, String[] wildcard, int from) {
        ArrayList<Pair<String, Mailbox>> matches = new ArrayList<>();

        // If the from is larger than the length of the wildcard, return the matches ( Empty Array )
        //  and stop the walking through the tree.
        if (from >= wildcard.length) {
            // If the previous wildcard was an '*', we know that we're dealing with an repeating acceptation
            //  of all the segments, so add all matches of index - 1
            if (wildcard[from - 1].equals("*"))
                matches.addAll(matchWildcard(hierarchy, a, wildcard, from - 1));

            return matches;
        }

        // Gets the current segment.
        String segment = wildcard[from];
        for (Mailbox b : a) {
            boolean aMatching = false;

            // Checks if the current segment is matching, either *
            //  %, a small wildcard, or a normal string.
            if (segment.equals("*"))
                aMatching = true;
            else if (segment.equals("%"))
                aMatching = true;
            else if (segment.contains("*") || segment.contains("%"))
                aMatching = matchNameWildcard(segment, b.getName());
            else if (segment.equals(b.getName()))
                aMatching = true;

            // Checks if it matches, and walks further into the tree, and adds
            //  the new prefix to the call.
            if (aMatching) {
                String hierarchyPrefix = (hierarchy != null ? hierarchy + '/'  : "");

                if (from + 1 >= wildcard.length)
                    matches.add(new Pair<String, Mailbox>(hierarchyPrefix + b.getName(), b));

                if (!b.getChildren().isEmpty() && !segment.endsWith("%"))
                    matches.addAll(matchWildcard(hierarchyPrefix + b.getName(), b.getChildren(),
                            wildcard, from + 1));
            }
        }

        return matches;
    }

    /**
     * Performs an wildcard search on the current tree.
     *
     * @param rawWildcard the raw wildcard.
     * @return the matches.
     */
    public ArrayList<Pair<String, Mailbox>> match(String rawWildcard) {
        String[] wildcard = rawWildcard.split("/");
        return matchWildcard(null, m_Heads, wildcard, 0);
    }

    /**
     * Computes all the Imap flags for the specified array of mailboxes.
     *
     * @param a the array of mailboxes.
     */
    private static void computeImapFlags(ArrayList<Mailbox> a) {
        for (Mailbox b : a) {
            if (b.getChildren().size() > 0) {
                b.getMeta().clearImapFlag(MailboxMeta.ImapFlags.HAS_NO_CHILDREN);
                b.getMeta().setImapFlag(MailboxMeta.ImapFlags.HAS_CHILDREN);

                computeImapFlags(b.getChildren());
            } else {
                b.getMeta().clearImapFlag(MailboxMeta.ImapFlags.HAS_CHILDREN);
                b.getMeta().setImapFlag(MailboxMeta.ImapFlags.HAS_NO_CHILDREN);
            }
        }
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    /**
     * Loops over all the mailboxes present, and computes the HasChildren & HasNoChildren flags
     */
    public void computeImapFlags() {
        m_ChangeBits |= HEADS_CHANGE_BIT;
        computeImapFlags(m_Heads);
    }

    public int getAndIncrementNextID() {
        m_ChangeBits |= NEXT_ID_CHANGE_BIT;
        return m_NextID++;
    }

    public ArrayList<Mailbox> getHeads() {
        return m_Heads;
    }

    public void setHeadsChangeBit() {
        m_ChangeBits |= HEADS_CHANGE_BIT;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    public static Mailboxes get(UUID accountUUID) {
        byte[] binaryUUID = ByteBuffer.wrap(new byte[16])
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(accountUUID.getMostSignificantBits())
                .putLong(accountUUID.getLeastSignificantBits())
                .array();

        Document document = DatabaseConnection
                .getInstance()
                .getMailboxesCollection()
                .find(Filters.eq("_id", new Binary(BsonBinarySubType.UUID_STANDARD, binaryUUID)))
                .first();

        if (document == null)
            return null;

        return fromDocument(document);
    }

    public static Mailboxes fromDocument(Document document) {
        return new Mailboxes((UUID) document.get("_id"),
                document.getList(HEADS_FIELD, Document.class)
                        .stream()
                        .map(Mailbox::fromDocument)
                        .collect(Collectors.toCollection(ArrayList::new)),
                document.getInteger(NEXT_ID_FIELD));
    }

    public void updateMailbox(String dir) {
        String[] segments = dir.split("/");
        Document update = new Document();

        ArrayList<Mailbox> a = m_Heads;

        int i = 0;
        for (String seg : segments) {
            Mailbox c = null;
            for (Mailbox b : a) {
                if (b.getName().equals(seg)) {
                    c = b;
                    break;
                }
            }

            if (c == null && ++i > segments.length)
                return;


        }



    }
}
