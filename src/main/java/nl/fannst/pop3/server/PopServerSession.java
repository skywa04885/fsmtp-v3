package nl.fannst.pop3.server;

import nl.fannst.datatypes.Pair;
import nl.fannst.models.accounts.BasicAccount;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.UUID;

public class PopServerSession {
    /****************************************************
     * Data Types
     ****************************************************/

    public static enum State {
        AUTHORIZATION, AUTHENTICATED
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private PrivateKey m_DecryptionKey;
    private State m_State;

    private BasicAccount m_User;

    private final ArrayList<Integer> m_DeletionMarkedMessages;
    private ArrayList<Pair<Integer, Integer>> m_Messages;
    private int m_HighestAccessNumber;

    /**
     * Creates new POP3 session instance
     */
    public PopServerSession() {
        m_State = State.AUTHORIZATION;
        m_HighestAccessNumber = 1;
        m_DeletionMarkedMessages = new ArrayList<Integer>();
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    public void reset() {
        m_DeletionMarkedMessages.clear();
        m_HighestAccessNumber = 1;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

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

    public void setMessages(ArrayList<Pair<Integer, Integer>> messages) {
        m_Messages = messages;
    }

    public ArrayList<Pair<Integer, Integer>> getMessages() {
        return m_Messages;
    }

    public void setHighestAccessNumber(int number) {
        m_HighestAccessNumber = number;
    }

    public int getHighestAccessNumber() {
        return m_HighestAccessNumber;
    }

    public void addDeleteMarkedMessage(int uid) {
        m_DeletionMarkedMessages.add(uid);
    }

    public ArrayList<Integer> getDeleteMarkedMessages() {
        return m_DeletionMarkedMessages;
    }
}
