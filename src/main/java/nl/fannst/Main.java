package nl.fannst;

import nl.fannst.imap.server.ImapPlainServer;
import nl.fannst.imap.server.ImapSecureServer;
import nl.fannst.net.secure.NioSSLServerConfig;
import nl.fannst.pop3.server.PlainTextPOP3Server;
import nl.fannst.pop3.server.SecurePOP3Server;
import nl.fannst.smtp.client.SmtpClient;
import nl.fannst.smtp.server.SmtpPlainServer;
import nl.fannst.smtp.server.SmtpSecureServer;
import nl.fannst.templates.FreeWriterRenderer;

import java.io.IOException;

public class Main {
    /* Other config */
    private static final String LISTEN = "0.0.0.0";

    /* SMTP Ports */
    private static final short SMTP_SSL_PORT = 465;
    private static final short SMTP_PLAIN_PORT = 25;

    /* POP3 Ports */
    private static final short POP3_SSL_PORT = 995;
    private static final short POP3_PLAIN_PORT = 110;

    /* IMAP Ports */
    private static final short IMAP_PLAIN_PORT = 143;
    private static final short IMAP_SSL_PORT = 993;

    /* Global shit */
    private static final String s_Protocol = "SSLv3";
    private static final Logger s_Logger = new Logger("Main", Logger.Level.INFO);
    private static final NioSSLServerConfig s_SSLServerConfig = new NioSSLServerConfig();

    /**
     * Starts the application.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        // Sets some default configuration
        s_SSLServerConfig.setServerKeyFile(System.getenv("SERVER_KEY_FILE"));
        s_SSLServerConfig.setServerKeyPass(System.getenv("SERVER_KEY_PASS"));
        s_SSLServerConfig.setServerStorePass(System.getenv("SERVER_STORE_PASS"));

        s_SSLServerConfig.setTrustFile(System.getenv("TRUST_FILE"));
        s_SSLServerConfig.setTrustStorePass(System.getenv("TRUST_STORE_PASS"));

        // Prints some introduction text
        s_Logger.log("Hello there! This server is NOT open source, all rights are reserved by Luke A.C.A. Rieff - Fannst Software.", Logger.Level.WARN);

        System.out.println("* I know, why is the repo visible than ? I want people to trust the source code,\r\n* and prove that I actually encrypt their messages.\r\n* "
            + "If there is any company out there,\r\n* I'm free for hiring (And ready to learn more).\r\n* I'm ready to learn new stuff ahha.\r\n* "
            + "For contact, try this email: lrieff@fannst.nl (Yes, Runs FSMTP/FIMAP/FPOP haha)");

        // Prepares for running
        prepare();
        prepareSMTPClient();

        // Runs the plain text servers
        runPlainPOP3();
        runPlainSMTP();
        runPlainIMAP();

        // Runs the secure servers
        runSecurePOP3();
        runSecureSMTP();
        runSecureIMAP();
    }

    private static void prepare() {
        String dbURI = System.getenv("MONGODB_URI");
        if (dbURI == null)
            dbURI = "mongodb://fannst.nl:27017/fannstv2";

        DatabaseConnection.createInstance(dbURI);

        try {
            FreeWriterRenderer.createInstance();
        } catch (IOException e) {
            s_Logger.log("Failed to create singleton instances: " + e.getMessage(), Logger.Level.FATAL);
        }
    }

    /****************************************************
     * SMTP
     ****************************************************/

    private static void prepareSMTPClient() {
        try {
            SmtpClient.createInstance();
            s_Logger.log("SMTPClient instance created!", Logger.Level.INFO);
        } catch (IOException e) {
            s_Logger.log("Failed to create SMTP client instance: " + e.getMessage(), Logger.Level.FATAL);
            System.exit(-1);
        }
    }

    private static void runSecureSMTP() {
        try {
            new SmtpSecureServer(s_SSLServerConfig, s_Protocol, LISTEN, SMTP_SSL_PORT);
            s_Logger.log("Secure SMTP instance created!", Logger.Level.INFO);
        } catch (Exception e) {
            s_Logger.log("Failed to create secure SMTP Instance: " + e.getMessage(), Logger.Level.FATAL);
            System.exit(-1);
        }
    }

    private static void runPlainPOP3() {
        try {
            new PlainTextPOP3Server(LISTEN, POP3_PLAIN_PORT);
            s_Logger.log("Plain SMTP instance created!", Logger.Level.INFO);
        } catch (IOException e) {
            s_Logger.log("Failed to create Plain SMTP Instance: " + e.getMessage(), Logger.Level.FATAL);
            System.exit(-1);
        }
    }

    /****************************************************
     * POP3
     ****************************************************/

    private static void runSecurePOP3() {
        try {
            new SecurePOP3Server(s_SSLServerConfig, s_Protocol, LISTEN, POP3_SSL_PORT);
            s_Logger.log("Secure POP3 instance created!", Logger.Level.INFO);
        } catch (Exception e) {
            s_Logger.log("Failed to create secure POP3 Instance: " + e.getMessage(), Logger.Level.FATAL);
            System.exit(-1);
        }
    }

    private static void runPlainSMTP() {
        try {
            new SmtpPlainServer(LISTEN, SMTP_PLAIN_PORT);
            s_Logger.log("Plain POP3 created!", Logger.Level.INFO);
        } catch (IOException e) {
            s_Logger.log("Failed to create Plain POP3 Instance: " + e.getMessage(), Logger.Level.FATAL);
            System.exit(-1);
        }
    }

    /****************************************************
     * IMAP
     ****************************************************/

    private static void runPlainIMAP() {
        try {
            new ImapPlainServer(LISTEN, IMAP_PLAIN_PORT);
            s_Logger.log("Plain IMAP created!", Logger.Level.INFO);
        } catch (IOException e) {
            s_Logger.log("Failed to create Plain IMAP Instance: " + e.getMessage(), Logger.Level.FATAL);
            System.exit(-1);
        }
    }

    private static void runSecureIMAP() {
        try {
            new ImapSecureServer(s_SSLServerConfig, s_Protocol, LISTEN, IMAP_SSL_PORT);
            s_Logger.log("Secure IMAP instance created!", Logger.Level.INFO);
        } catch (Exception e) {
            s_Logger.log("Failed to create secure IMAP Instance: " + e.getMessage(), Logger.Level.FATAL);
            System.exit(-1);
        }
    }
}
