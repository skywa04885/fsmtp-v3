package nl.fannst.mime;

import java.util.HashMap;
import java.util.Locale;

public enum ContentType {
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    BINARY("application/octet-stream"),
    MULTIPART_ALTERNATIVE( "multipart/alternative");

    private final String m_TypeString;
    private final int m_HashCode;

    ContentType(String typeString) {
        m_TypeString = typeString;
        m_HashCode = typeString.hashCode();
    }

    public String getTypeString() {
        return m_TypeString;
    }

    public int getHashCode() {
        return m_HashCode;
    }

    /**
     * Gets an content type by type string
     * @param typeString the type string
     * @return the content type, or null
     */
    public static ContentType getByTypeString(String typeString) {
        int hashCode = typeString.toLowerCase(Locale.ROOT).hashCode();

        for (ContentType contentType : ContentType.values()) {
            if (contentType.getHashCode() == hashCode) {
                return contentType;
            }
        }

        return null;
    }

    public String withBoundary(String boundary) {
        return getTypeString() + "; boundary=\"" + boundary + '"';
    }
}