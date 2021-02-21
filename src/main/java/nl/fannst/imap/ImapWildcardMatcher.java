package nl.fannst.imap;

import java.util.regex.Pattern;

public class ImapWildcardMatcher {

    // */* ->           Folder/SmallFolder
    // */*/* ->         Folder/SmallFolder/TinyFolder
    // Folder/* ->      Folder/SmallFolder1 & Folder/SmallFolder2/TinyFolder1
    // Folder/%         Folder/SmallFolder1 & Folder/SmallFolder2
    // %                Folder1 & Folder2
    // Folder/Folder*   Folder/FolderA & Folder/FolderB ( but not, Folder/A )

    public static Pattern createPattern(String wildcard) {
        StringBuilder regexp = new StringBuilder();
        regexp.append("^(");

        String[] segments = wildcard.split("/");
        int i = 0;
        for (String segment : segments) {
            if (segment.equals("*")) {
                regexp.append("(([^/])+)");
            } else if (segment.equals("%")) {
                regexp.append("(([^/])+)");
                break;
            } else if (segment.contains("*")) {
                String[] subSegments = segment.split("\\*");

                regexp.append("(");
                int j = 0;
                for (String subSegment : subSegments) {
                    regexp.append("(").append(subSegment).append(")");
                    regexp.append("([^/])+");
                }
                regexp.append(")");
            } else {
                regexp.append("(").append(segment).append(")");
            }

            if (++i < segments.length) {
                regexp.append("/");
            }
        }

        regexp.append(")$");

        return Pattern.compile(regexp.toString());
    }

    public static boolean matches(String raw, String wildcard) {
        String[] wildcardSegments = wildcard.split("/");
        String[] rawSegments = raw.split("/");

        for (int i = 0; i < wildcardSegments.length; ++i) {
            if (i >= rawSegments.length) break;

            String wildcardSegment = wildcardSegments[i];
            String rawSegment = rawSegments[i];

            if (wildcardSegment.equals("*")) {
                if (i + 1 >= wildcardSegments.length)
                    return true;
            } else if (wildcardSegment.equals("%")) {
                return i + 1 >= rawSegments.length;
            } else if (!wildcardSegment.equals(rawSegment)) {
                return false;
            }
        }

        return false;
    }
}
