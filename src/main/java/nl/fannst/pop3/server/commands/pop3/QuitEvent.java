package nl.fannst.pop3.server.commands.pop3;

import nl.fannst.models.mail.Message;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.PopReply;
import nl.fannst.pop3.server.PopServerSession;
import nl.fannst.pop3.server.commands.PopCommandHandler;

public class QuitEvent implements PopCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {

    }

    @Override
    public void handle(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        new PopReply(PopReply.Indicator.OK, "Fannst POP3 Server signing off.").write(client);
        client.close();

        if (session.getDeleteMarkedMessages().size() > 0) {
            new Thread(() -> {
                Message.deleteMany(session.getAuthenticationUser().getUUID(), session.getDeleteMarkedMessages());
            }).start();
        }
    }
}
