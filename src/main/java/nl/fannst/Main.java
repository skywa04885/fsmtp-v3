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
import org.apache.commons.cli.*;

import javax.xml.crypto.Data;
import java.io.IOException;

public class Main {
    /* Other config */
    private static final String LISTEN = "0.0.0.0";

    /* SMTP Ports */
    private static short SMTP_SSL_PORT = 465;
    private static short SMTP_PLAIN_PORT = 25;
    private static boolean SMTP_RUN_SSL = false;
    private static boolean SMTP_RUN_PLAIN = false;

    /* POP3 Ports */
    private static short POP3_SSL_PORT = 995;
    private static short POP3_PLAIN_PORT = 110;
    private static boolean POP3_RUN_SSL = false;
    private static boolean POP3_RUN_PLAIN = false;

    /* IMAP Ports */
    private static short IMAP_PLAIN_PORT = 143;
    private static short IMAP_SSL_PORT = 993;
    private static boolean IMAP_RUN_SSL = false;
    private static boolean IMAP_RUN_PLAIN = false;

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
        parseArguments(args);

        // Prints some stuff.
        s_Logger.log("Programmed by Luke A.C.A. Rieff so yeah... Gonna blow up!", Logger.Level.WARN);

        // Sets some default configuration
        s_SSLServerConfig.setServerKeyFile(System.getenv("SERVER_KEY_FILE"));
        s_SSLServerConfig.setServerKeyPass(System.getenv("SERVER_KEY_PASS"));
        s_SSLServerConfig.setServerStorePass(System.getenv("SERVER_STORE_PASS"));

        s_SSLServerConfig.setTrustFile(System.getenv("TRUST_FILE"));
        s_SSLServerConfig.setTrustStorePass(System.getenv("TRUST_STORE_PASS"));

        // Prepares for running
        prepare();
        prepareSMTPClient();

        // Runs the plain text servers
        if (POP3_RUN_PLAIN)
            runPlainPOP3();
        if (SMTP_RUN_PLAIN)
            runPlainSMTP();
        if (IMAP_RUN_PLAIN)
            runPlainIMAP();

        // Runs the secure servers
        if (POP3_RUN_SSL)
            runSecurePOP3();
        if (SMTP_RUN_SSL)
            runSecureSMTP();
        if (IMAP_RUN_SSL)
            runSecureIMAP();
    }

    private static void parseArguments(String[] args) {
        Options options = new Options();

        // Arguments to specify which servers to run.

        Option runPop3Ssl = new Option("r_pop3_s", "run_pop3_ssl", false, "Run POP3 SSL Server");
        options.addOption(runPop3Ssl);
        Option runPop3Plain = new Option("r_pop3_p", "run_pop3_plain", false, "Run POP3 Plain Server");
        options.addOption(runPop3Plain);

        Option runImapSsl = new Option("r_imap_s", "run_imap_ssl", false, "Run IMAP SSL Server");
        options.addOption(runImapSsl);
        Option runImapPlain = new Option("r_imap_p", "run_imap_plain", false, "Run IMAP Plain Server");
        options.addOption(runImapPlain);

        Option runSmtpSsl = new Option("r_smtp_s", "run_smtp_ssl", false, "Run SMTP SSL Server");
        options.addOption(runSmtpSsl);
        Option runSmtpPlain = new Option("r_smtp_p", "run_smtp_plain", false, "Run SMTP Plain Server");
        options.addOption(runSmtpPlain);

        // Arguments to specify server ports.

        Option pop3Port = new Option ("pop3", "pop3_port", true, "POP3 Port SSL, Plain");
        pop3Port.setRequired(false);
        pop3Port.setArgs(2);
        pop3Port.setArgName("SSL Port, Plain Port");
        pop3Port.setValueSeparator(',');
        options.addOption(pop3Port);

        Option smtpPort = new Option("smtp", "smtp_port", true, "SMTP Port SSL, Plain");
        smtpPort.setRequired(false);
        smtpPort.setArgs(2);
        smtpPort.setArgName("SSL Port, Plain Port");
        smtpPort.setValueSeparator(',');
        options.addOption(smtpPort);

        Option imapPort = new Option("imap", "imap_port", true, "IMAP Port SSL, Plain");
        imapPort.setRequired(false);
        imapPort.setArgs(2);
        imapPort.setArgName("SSL Port, Plain Port");
        imapPort.setValueSeparator(',');
        options.addOption(imapPort);

        // Performs the command parsing.

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = null;

        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());

            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("[exec] <args ...>", options);

            System.exit(1);
        }

        // Reads the arguments.

        if (commandLine.hasOption("run_pop3_ssl"))
            POP3_RUN_SSL = true;
        if (commandLine.hasOption("run_pop3_plain"))
            POP3_RUN_PLAIN = true;

        if (commandLine.hasOption("run_imap_ssl"))
            IMAP_RUN_SSL = true;
        if (commandLine.hasOption("run_imap_plain"))
            IMAP_RUN_PLAIN = true;

        if (commandLine.hasOption("run_smtp_ssl"))
            SMTP_RUN_SSL = true;
        if (commandLine.hasOption("run_smtp_plain"))
            SMTP_RUN_PLAIN = true;

        if (commandLine.hasOption("smtp") && commandLine.getOptionValues("smtp").length == 2) {
            String ssl = commandLine.getOptionValues("smtp")[0];
            String plain = commandLine.getOptionValues("smtp")[1];

            try {
                SMTP_SSL_PORT = Short.parseShort(ssl);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            try {
                SMTP_PLAIN_PORT = Short.parseShort(plain);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption("pop3") && commandLine.getOptionValues("pop3").length == 2) {
            String ssl = commandLine.getOptionValues("pop3")[0];
            String plain = commandLine.getOptionValues("pop3")[1];

            try {
                POP3_SSL_PORT = Short.parseShort(ssl);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            try {
                POP3_PLAIN_PORT = Short.parseShort(plain);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption("imap") && commandLine.getOptionValues("imap").length == 2) {
            String ssl = commandLine.getOptionValues("imap")[0];
            String plain = commandLine.getOptionValues("imap")[1];

            try {
                IMAP_SSL_PORT = Short.parseShort(ssl);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            try {
                IMAP_PLAIN_PORT = Short.parseShort(plain);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
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
