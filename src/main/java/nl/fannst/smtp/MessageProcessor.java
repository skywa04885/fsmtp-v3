package nl.fannst.smtp;

import nl.fannst.datatypes.Pair;
import nl.fannst.dkim.DKIMResult;
import nl.fannst.dmarc.DMARCHeader;
import nl.fannst.dmarc.DMARCResult;
import nl.fannst.dmarc.DMARCValidator;
import nl.fannst.mime.Address;
import nl.fannst.mime.Header;
import nl.fannst.net.ip.CIDR_IPv4Address;
import nl.fannst.smtp.headers.AuthenticationResults;
import nl.fannst.smtp.headers.Received;
import nl.fannst.smtp.server.session.SmtpSessionProtocol;
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
    private final String m_Body, m_OriginalHeaders;
    private Address m_From;

    /**
     * Creates new message processor instance
     *
     * @param raw the raw message
     * @param greetingName the name specified in greeting
     * @param mailFrom the from specified in smtp
     * @param remoteAddress the remote address
     * @throws Header.InvalidHeaderException possible invalid header
     * @throws Address.InvalidAddressException possible invalid address
     */
    public MessageProcessor(String raw, String greetingName, Address mailFrom, InetAddress remoteAddress) throws Header.InvalidHeaderException, Address.InvalidAddressException {
        Pair<String, String> res = Header.splitHeadersAndBody(new Scanner(raw));

        m_OriginalHeaders = res.getFirst();
        m_Body = res.getSecond();

        // Loops over the headers, and checks if any of them contain the from keyword
        //  which we will use for ADKIM.
        for (Header header : Header.parseHeaders(new Scanner(m_OriginalHeaders))) {
            if (header.getKey().equals("from")) {
                m_From = Address.parse(header.getValue());
            }
        }

        m_MailFrom = mailFrom;
        m_GreetingName = greetingName;
        m_RemoteAddress = remoteAddress;
        m_Headers = new ArrayList<Header>();
    }

    /**
     * Performs the message validation (SPF, DKIM, DMARC)
     *
     * @param relay is relayed ?
     * @return valid or not
     */
    public boolean validate(boolean relay) {
        m_Headers.add(new Header(Received.KEY, new Received(m_RemoteAddress, m_GreetingName, SmtpSessionProtocol.ESMTP, new Date()).toString()));
        if (relay) return true;

        String domain = m_From.getWithoutSubdomain();
        CIDR_IPv4Address address = new CIDR_IPv4Address(m_RemoteAddress.getAddress(), (byte) -1);

        // Validates SPF.
        SPFValidator spfValidator = new SPFValidator(domain, address);
        SPFResult spfValidatorResult = spfValidator.validate();
        String spfFeedback = spfValidator.getFeedback();

        // Validates DKIM.
        DKIMResult dkimResult = DKIMResult.PASS;
        String dkimFeedback = "not implemented";

        // Validates DMARC.
        DMARCValidator dmarcValidator = new DMARCValidator(m_From, m_MailFrom);
        DMARCResult dmarcResult = dmarcValidator.validate(spfValidatorResult, dkimResult);
        String dmarcFeedback = dmarcValidator.getFeedback();

        // Returns false if rejected.
        if (dmarcResult == DMARCResult.REJECTED) return false;

        // Adds the result headers
        m_Headers.add(new Header(SPFHeader.KEY, new SPFHeader(m_RemoteAddress, spfValidatorResult, spfFeedback).toString()));
        m_Headers.add(new Header(DMARCHeader.KEY, new DMARCHeader(m_RemoteAddress, dmarcResult, dmarcFeedback).toString()));
        m_Headers.add(new Header(AuthenticationResults.key, new AuthenticationResults(
                dkimResult, spfValidatorResult, dmarcResult,
                dkimFeedback, spfFeedback, dmarcFeedback
        ).toString()));

        return true;
    }

    /**
     * Builds the result processed message
     *
     * @return the processed message
     */
    public String build() {
        return m_OriginalHeaders +
                Header.buildHeaders(m_Headers) +
                "\r\n" +
                m_Body;
    }
}
