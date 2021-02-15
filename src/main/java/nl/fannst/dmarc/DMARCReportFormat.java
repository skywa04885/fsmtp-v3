package nl.fannst.dmarc;

public enum DMARCReportFormat {
    AUTH_FAIL_REPORT_FORMAT("afrf");

    public static final String KEY = "v";

    private final String m_Keyword;

    DMARCReportFormat(String keyword) {
        m_Keyword = keyword;
    }

    public String getKeyword() {
        return m_Keyword;
    }

    public static DMARCReportFormat fromString(String raw) {
        for (DMARCReportFormat format : DMARCReportFormat.values()) {
            if (format.getKeyword().equalsIgnoreCase(raw)) {
                return format;
            }
        }

        return null;
    }
}
