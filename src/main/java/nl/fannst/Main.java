package nl.fannst;

import nl.fannst.mime.Address;
import nl.fannst.net.secure.NioSSLServerConfig;
import nl.fannst.pop3.server.PlainTextPOP3Server;
import nl.fannst.pop3.server.SecurePOP3Server;
import nl.fannst.smtp.MessageProcessor;
import nl.fannst.smtp.client.SmtpClient;
import nl.fannst.smtp.server.PlainTextSMTPServer;
import nl.fannst.smtp.server.SecureSMTPServer;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class Main {
    /* Other config */
    private static final String LISTEN = "0.0.0.0";

    /* SMTP Ports */
    private static final short SMTP_SSL_PORT = 465;
    private static final short SMTP_PLAIN_PORT = 25;

    /* POP3 Ports */
    private static final short POP3_SSL_PORT = 995;
    private static final short POP3_PLAIN_PORT = 110;

    /* Global shit */
    private static final Logger m_Logger = new Logger("Main", Logger.Level.INFO);
    private static final NioSSLServerConfig m_SSLServerConfig = new NioSSLServerConfig();

    /**
     * Starts the application
     * @param args the command line arguments
     * @throws Exception possible exception which may occur.
     */
    public static void main(String[] args) throws Exception {
        m_SSLServerConfig.setServerKeyFile(System.getenv("SERVER_KEY_FILE"));
        m_SSLServerConfig.setServerKeyPass(System.getenv("SERVER_KEY_PASS"));
        m_SSLServerConfig.setServerStorePass(System.getenv("SERVER_STORE_PASS"));

        m_SSLServerConfig.setTrustFile(System.getenv("TRUST_FILE"));
        m_SSLServerConfig.setTrustStorePass(System.getenv("TRUST_STORE_PASS"));

        // Prepares for running
        prepare();
        prepareSMTPClient();

        // Runs the plain text servers
        runPlainPOP3();
        runPlainSMTP();

        // Runs the secure servers
        runSecurePOP3();
        runSecureSMTP();

    }

    private static void prepare() {
        DatabaseConnection.connect("mongodb://fannst.nl:27017/fannst", "fannst");
    }

    private static void prepareSMTPClient() {
        try {
            SmtpClient.createInstance();
            m_Logger.log("SMTPClient instance created!", Logger.Level.INFO);
        } catch (IOException e) {
            m_Logger.log("Failed to create SMTP client instance: " + e.getMessage(), Logger.Level.FATAL);
            System.exit(-1);
        }
    }

    private static void runSecureSMTP() {
        try {
            new SecureSMTPServer(m_SSLServerConfig, "TLSv1.2", LISTEN, SMTP_SSL_PORT);
            m_Logger.log("Secure SMTP instance created!", Logger.Level.INFO);
        } catch (Exception e) {
            m_Logger.log("Failed to create secure SMTP Instance: " + e.getMessage(), Logger.Level.FATAL);
            System.exit(-1);
        }
    }

    private static void runPlainPOP3() {
        try {
            new PlainTextPOP3Server(LISTEN, POP3_PLAIN_PORT);
            m_Logger.log("Plain SMTP instance created!", Logger.Level.INFO);
        } catch (IOException e) {
            m_Logger.log("Failed to create Plain SMTP Instance: " + e.getMessage(), Logger.Level.FATAL);
            System.exit(-1);
        }
    }

    private static void runSecurePOP3() {
        try {
            new SecurePOP3Server(m_SSLServerConfig, "TLSv1.2", LISTEN, POP3_SSL_PORT);
            m_Logger.log("Secure POP3 instance created!", Logger.Level.INFO);
        } catch (Exception e) {
            m_Logger.log("Failed to create secure POP3 Instance: " + e.getMessage(), Logger.Level.FATAL);
            System.exit(-1);
        }
    }

    private static void runPlainSMTP() {
        try {
            new PlainTextSMTPServer(LISTEN, SMTP_PLAIN_PORT);
            m_Logger.log("Plain POP3 created!", Logger.Level.INFO);
        } catch (IOException e) {
            m_Logger.log("Failed to create Plain POP3 Instance: " + e.getMessage(), Logger.Level.FATAL);
            System.exit(-1);
        }
    }
}
