package nl.fannst.pop3.server.commands;

import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.server.PopServerSession;

public class PopCommandRequirement {
    public static void mustBeAuthorizationState(PopServerSession session) throws PopCommand.SequenceException {
        if (session.getState() != PopServerSession.State.AUTHORIZATION) {
            throw new PopCommand.SequenceException("Already authenticated, perform RSET first.");
        }
    }

    public static void mustBeAuthenticatedState(PopServerSession session) throws PopCommand.SequenceException {
        if (session.getState() != PopServerSession.State.AUTHENTICATED) {
            throw new PopCommand.SequenceException("AUTH First.");
        }
    }
}
