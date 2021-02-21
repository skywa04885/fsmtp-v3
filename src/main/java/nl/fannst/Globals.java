package nl.fannst;

import nl.fannst.mime.Address;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class Globals {
    public static final SimpleDateFormat MIME_DATE_FORMAT = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z (z)");

    public static final Address DELIVERY_SYSTEM_ADDRESS = new Address("delivery@skynet.fannst.nl", "Fannst Mail");

    public static final Pattern MAILBOX_PATTERN = Pattern.compile("^([A-Za-z0-9-_\\[\\]/]+)$");
}
