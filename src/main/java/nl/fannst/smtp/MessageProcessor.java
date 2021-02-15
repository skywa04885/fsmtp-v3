package nl.fannst.smtp;

import nl.fannst.dkim.DKIMResult;
import nl.fannst.dmarc.DMARCHeader;
import nl.fannst.dmarc.DMARCResult;
import nl.fannst.dmarc.DMARCValidator;
import nl.fannst.mime.Address;
import nl.fannst.mime.Header;
import nl.fannst.net.ip.CIDR_IPv4Address;
import nl.fannst.smtp.headers.AuthenticationResults;
import nl.fannst.smtp.headers.Received;
import nl.fannst.smtp.server.SmtpSessionProtocol;
import nl.fannst.spf.SPFHeader;
import nl.fannst.spf.SPFResult;
import nl.fannst.spf.SPFValidator;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class MessageProcessor {
    private final ArrayList<Header> m_Headers;
    private final String m_GreetingName;
    private final Address m_MailFrom;
    private final InetAddress m_RemoteAddress;
    private final StringBuilder m_Body;
    private Address m_From;

    public MessageProcessor(String raw, String greetingName, Address mailFrom, InetAddress remoteAddress) throws Header.InvalidHeaderException, Address.InvalidAddressException {
        Scanner scanner = new Scanner(raw);

        m_Headers = Header.parseHeaders(scanner);
        for (Header header : m_Headers) {
            if (header.getKey().equals("from")) {
                m_From = Address.parse(header.getValue());
            }
        }

        m_Body = new StringBuilder();
        while (scanner.hasNextLine()) {
            m_Body.append(scanner.nextLine()).append("\r\n");
        }

        m_MailFrom = mailFrom;
        m_GreetingName = greetingName;
        m_RemoteAddress = remoteAddress;
    }

    public boolean process() {
        String domain = m_From.getWithoutSubdomain();
        CIDR_IPv4Address address = new CIDR_IPv4Address(m_RemoteAddress.getAddress(), (byte) -1);

        //
        // Validates SPF
        //

        SPFValidator spfValidator = new SPFValidator(domain, address);
        SPFResult spfValidatorResult = spfValidator.validate();
        String spfFeedback = spfValidator.getFeedback();

        //
        // Validates DKIM
        //

        DKIMResult dkimResult = DKIMResult.PASS;
        String dkimFeedback = "not implemented";

        //
        // Validates DMARC
        //

        DMARCValidator dmarcValidator = new DMARCValidator(m_From, m_MailFrom);
        DMARCResult dmarcResult = dmarcValidator.validate(spfValidatorResult, dkimResult);
        String dmarcFeedback = dmarcValidator.getFeedback();

        if (dmarcResult == DMARCResult.REJECTED) {
            System.out.println(dmarcFeedback);
            return false;
        }

        //
        // Adds result headers
        //

        m_Headers.removeIf(header -> header.getKey().equalsIgnoreCase("x-mailer"));

        m_Headers.add(new Header("x-mailer", "FSMTP Version 3 - By Luke A.C.A. Rieff"));
        m_Headers.add(new Header(SPFHeader.KEY, new SPFHeader(m_RemoteAddress, spfValidatorResult, spfFeedback).toString()));
        m_Headers.add(new Header(DMARCHeader.KEY, new DMARCHeader(m_RemoteAddress, dmarcResult, dmarcFeedback).toString()));
        m_Headers.add(new Header(AuthenticationResults.key, new AuthenticationResults(
                dkimResult, spfValidatorResult, dmarcResult,
                dkimFeedback, spfFeedback, dmarcFeedback
        ).toString()));
        m_Headers.add(new Header(Received.KEY, new Received(m_RemoteAddress, m_GreetingName, SmtpSessionProtocol.ESMTP, new Date()).toString()));

        return true;
    }

    public String build() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(Header.buildHeaders(m_Headers));
        stringBuilder.append("\r\n").append(m_Body);

        return stringBuilder.toString();
    }
}
