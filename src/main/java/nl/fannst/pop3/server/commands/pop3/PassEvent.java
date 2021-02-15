package nl.fannst.pop3.server.commands.pop3;

import nl.fannst.auth.Passwords;
import nl.fannst.datatypes.Pair;
import nl.fannst.encryption.AES;
import nl.fannst.encryption.RSA;
import nl.fannst.models.accounts.BasicAccount;
import nl.fannst.models.mail.Message;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.PopReply;
import nl.fannst.pop3.server.PopServerSession;
import nl.fannst.pop3.server.commands.PopCommandRequirement;
import nl.fannst.pop3.server.commands.PopCommandHandler;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

public class PassEvent implements PopCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommandRequirement.mustBeAuthorizationState(session);

        if (session.getAuthenticationUser() == null) {
            throw new PopCommand.SequenceException("USER first.");
        }
    }

    @Override
    public void handle(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommand.PASS_Argument argument = PopCommand.PASS_Argument.parse(command.getArguments());

        if (!Passwords.verify(session.getAuthenticationUser().getPassword(), argument.getPassword()))  {
            new PopReply(PopReply.Indicator.ERR, PopReply.ResponseCode.AUTH, "Invalid credentials.").write(client);
            return;
        }

        new PopReply(PopReply.Indicator.OK, "Welcome back, " + session.getAuthenticationUser().getFulLName()).write(client);
        session.setState(PopServerSession.State.AUTHENTICATED);

        // Parses the key
        String rsa_private = BasicAccount.getPrivateKey(session.getAuthenticationUser().getUUID());
        if (rsa_private == null) {
            throw new Exception("Failed to get private key for user!");
        }

        String[] segments = rsa_private.split("\\.");
        if (segments.length != 3) {
            throw new Exception("Invalid private key in user!");
        }

        byte[] rawCipherText = Base64.getDecoder().decode(segments[0].getBytes());
        byte[] rawIV = Base64.getDecoder().decode(segments[1].getBytes());
        byte[] rawSalt = Base64.getDecoder().decode(segments[2].getBytes());

        SecretKey key = AES.deriveKey(argument.getPassword().toCharArray(), rawSalt, 1000, 32);

        byte[] decrypted = AES.decrypt(rawCipherText, key, rawIV);
        assert decrypted != null;

        PrivateKey privateKey = RSA.parsePrivateKey(decrypted);
        assert (privateKey != null);

        session.setDecryptionKey(privateKey);

        // Gets the currently available message
        ArrayList<Pair<UUID, Integer>> messages = Message.getPOP3BasicInfo(session.getAuthenticationUser().getUUID());
        session.setMessages(messages);
    }
}
