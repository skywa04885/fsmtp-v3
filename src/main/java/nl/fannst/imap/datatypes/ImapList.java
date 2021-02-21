package nl.fannst.imap.datatypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ImapList<T> extends ImapDataType {
    /****************************************************
     * Data Types
     ****************************************************/

    public static class SyntaxException extends Exception {
        public SyntaxException(String s) {
            super(s);
        }
    }


    /*
        Data structures are represented as a "parenthesized list"; a sequence
        of data items, delimited by space, and bounded at each end by
        parentheses.  A parenthesized list can contain other parenthesized
        lists, using multiple levels of parentheses to indicate nesting.

        The empty list is represented as () -- a parenthesized list with no
        members.
    */

    private static final char PREFIX = '(';
    private static final char SUFFIX = ')';
    private static final char DELIMITER = ' ';

    private final List<T> m_List;

    public ImapList(List<T> list) {
        m_List = list;
    }

    @Override
    public String toString() {
        // If empty, just return PREFIX + SUFFIX.
        if (m_List.isEmpty())
            return "" + PREFIX + SUFFIX;

        // Loops over all the elements, and appends the string version to the
        //  final list.
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PREFIX);
        for (int i = 0; i < m_List.size(); ++i) {
            T elem = m_List.get(i);
            stringBuilder.append(elem.toString());

            if (i + 1 < m_List.size())
                stringBuilder.append(DELIMITER);
        }

        stringBuilder.append(SUFFIX);
        return stringBuilder.toString();
    }

    public static ImapList<String> parse(String raw) throws SyntaxException {
        ArrayList<String> result = new ArrayList<String>();

        if (raw.charAt(0) != PREFIX || raw.charAt(raw.length() - 1) != SUFFIX)
            throw new SyntaxException("invalid list");

        try (Scanner scanner = new Scanner(raw.substring(1, raw.length() - 1)).useDelimiter("\\s+")) {
            while (scanner.hasNext())
                result.add(scanner.next());
        }

        return new ImapList<String>(result);
    }

    public List<T> getList() {
        return m_List;
    }
}
