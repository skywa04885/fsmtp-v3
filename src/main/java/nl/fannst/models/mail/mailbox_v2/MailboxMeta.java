package nl.fannst.models.mail.mailbox_v2;

import org.bson.Document;

public class MailboxMeta {
    private static final String MESSAGE_COUNT_FIELD = "m_cnt";
    private static final String FLAGS_FIELD = "f";

    private int m_MessageCount;
    private int m_Flags;

    public Document toDocument() {
        Document document = new Document();

        document.append(MESSAGE_COUNT_FIELD, m_MessageCount);
        document.append(FLAGS_FIELD, m_Flags);

        return document;
    }
}
