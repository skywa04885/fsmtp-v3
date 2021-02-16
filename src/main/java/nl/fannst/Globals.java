package nl.fannst;

import nl.fannst.mime.Address;

import java.text.SimpleDateFormat;

public class Globals {
    public static final SimpleDateFormat MIME_DATE_FORMAT = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z (z)");

    public static final Address DELIVERY_SYSTEM_ADDRESS = new Address("delivery@skynet.fannst.nl", "Fannst Mail");
}
