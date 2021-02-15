package nl.fannst.dmarc;

import java.util.ArrayList;

public enum DMARCFailureOptions {
    ALL_FAIL_REPORT("0"),
    ANY_FAIL_REPORT("1"),
    SIGNATURE_FAIL_REPORT("d"),
    SPF_FAIL_REPORT("s");

    private final String m_Keyword;

    DMARCFailureOptions(String keyword) {
        m_Keyword = keyword;
    }

    public String getKeyword() {
        return m_Keyword;
    }

    public static DMARCFailureOptions fromString(String raw) {
        for (DMARCFailureOptions option : DMARCFailureOptions.values()) {
            if (option.getKeyword().equalsIgnoreCase(raw)) {
                return option;
            }
        }

        return null;
    }

    public static ArrayList<DMARCFailureOptions> parseOptions(String raw) {
        ArrayList<DMARCFailureOptions> options = new ArrayList<DMARCFailureOptions>();

        String[] segments = raw.split(":");
        for (String segment : segments) {
            DMARCFailureOptions option;
            if ((option = fromString(segment)) != null) {
                options.add(option);
            }
        }

        return options;
    }
}
