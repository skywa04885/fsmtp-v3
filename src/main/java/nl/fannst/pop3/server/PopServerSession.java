package nl.fannst.pop3.server;

import nl.fannst.datatypes.Pair;
import nl.fannst.datatypes.SegmentedBuffer;
import nl.fannst.models.accounts.BasicAccount;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

public class PopServerSession {
    public static enum State {
        AUTHORIZATION, AUTHENTICATED
    }

    private PrivateKey m_DecryptionKey;
    private State m_State;

    /* Authentication */
    private BasicAccount m_User;

    /* Messages */
    private ArrayList<Pair<UUID, Integer>> m_Messages;
    private ArrayList<UUID> m_DeleteMarkedMessages;
    private int m_HighestAccessNumber;

    public PopServerSession() {
        m_State = State.AUTHORIZATION;
        m_HighestAccessNumber = 1;
        m_DeleteMarkedMessages = new ArrayList<UUID>();
    }


    public void reset() {
        m_DeleteMarkedMessages.clear();
        m_HighestAccessNumber = 1;
    }

    public PrivateKey getDecryptionKey() {
        return m_DecryptionKey;
    }

    public void setDecryptionKey(PrivateKey key) {
        m_DecryptionKey = key;
    }

    public State getState() {
        return m_State;
    }

    public void setState(State state) {
        m_State = state;
    }

    public void setAuthenticationUser(BasicAccount user) {
        m_User = user;
    }

    public BasicAccount getAuthenticationUser() {
        return m_User;
    }

    public void setMessages(ArrayList<Pair<UUID, Integer>> messages) {
        m_Messages = messages;
    }

    public ArrayList<Pair<UUID, Integer>> getMessages() {
        return m_Messages;
    }

    public void setHighestAccessNumber(int number) {
        m_HighestAccessNumber = number;
    }

    public int getHighestAccessNumber() {
        return m_HighestAccessNumber;
    }

    public void addDeleteMarkedMessage(UUID uuid) {
        m_DeleteMarkedMessages.add(uuid);
    }

    public ArrayList<UUID> getDeleteMarkedMessages() {
        return m_DeleteMarkedMessages;
    }
}
