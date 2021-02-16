package nl.fannst.mime.composer;

import nl.fannst.mime.ContentType;
import nl.fannst.mime.Header;
import nl.fannst.mime.TransferEncoding;
import nl.fannst.mime.encoding.QuotedPrintable;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class ComposeTextSection extends ComposeSection {
    private final TransferEncoding m_TransferEncoding;
    private final ContentType m_ContentType;
    private final String m_RawBody;

    public ComposeTextSection(TransferEncoding transferEncoding, ContentType contentType, String body) {
        m_ContentType = contentType;
        m_TransferEncoding = transferEncoding;
        m_RawBody = body;
    }

    @Override
    public String build() {
        StringBuilder stringBuilder = new StringBuilder();

        // Builds the section headers.
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new Header("Content-Type", m_ContentType.getTypeString()));
        headers.add(new Header("Content-Transfer-Encoding", m_TransferEncoding.getKeyWord()));
        stringBuilder.append(Header.buildHeaders(headers)).append("\r\n");

        // Encodes the body, and appends it
        switch (m_TransferEncoding) {
            case BASE64 -> stringBuilder.append(Base64.getEncoder().encodeToString(m_RawBody.getBytes()));
            case QUOTED_PRINTABLE -> stringBuilder.append(QuotedPrintable.encode(m_RawBody));
        }
        stringBuilder.append("\r\n");

        // Returns the built body
        return stringBuilder.toString();
    }
}
