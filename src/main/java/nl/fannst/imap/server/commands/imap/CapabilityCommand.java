package nl.fannst.imap.server.commands.imap;

import nl.fannst.imap.ImapCapability;
import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.server.commands.ImapCommandHandler;
import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;

public class CapabilityCommand implements ImapCommandHandler {
    public static final ImapCapability[] NON_AUTHENTICATED_CAPABILITIES = {
            new ImapCapability(ImapCapability.Type.IMAP4rev1),
            new ImapCapability(ImapCapability.AuthMechanism.PLAIN)
    };

    public static final ImapCapability[] AUTHENTICATED_CAPABILITIES = {
            new ImapCapability(ImapCapability.Type.IMAP4rev1)
    };

    @Override
    public void allowed(NIOClientWrapperArgument client, ImapCommand command) throws Exception {

    }

    @Override
    public void handle(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        send(client, command.getSequenceNo(), NON_AUTHENTICATED_CAPABILITIES);
        new ImapResponse(command.getSequenceNo(), ImapResponse.Type.OK, "That's all we can do.").write(client);
    }

    /**
     * Sends an list of capabilities to the client.
     * @param client the client
     * @param sequenceNo the sequence number
     * @param capabilities the capabilities
     * @throws IOException possible IO exception
     */
    public static void send(NIOClientWrapperArgument client, String sequenceNo, ImapCapability[] capabilities) throws IOException {
        StringBuilder capabilityString = new StringBuilder();

        // Builds the capability response.
        for (int i = 0; i < capabilities.length; ++i) {
            capabilityString.append(capabilities[i].toString());
            if (i + 1 < capabilities.length)
                capabilityString.append(' ');
        }

        // Sends the response to the client.
        new ImapResponse(null, ImapResponse.Type.CAPABILITY, capabilityString.toString(), false).write(client);
    }
}
