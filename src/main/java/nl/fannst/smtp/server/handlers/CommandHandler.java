package nl.fannst.smtp.server.handlers;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.smtp.SmtpCommand;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.session.SmtpServerSession;
import nl.fannst.smtp.server.commands.SmtpCommandEvent;

import java.io.IOException;

public class CommandHandler {
    /**
     * Handles an command which has been sent by the client
     *
     * @param client the client
     * @param session the session
     * @param line the line containing command
     */
    public static void handle(NIOClientWrapperArgument client, SmtpServerSession session, String line) throws IOException {
        try {
            // Attempts to parse the command, otherwise catch exception on the outside.
            SmtpCommand command = SmtpCommand.parse(line.trim());

            // Gets the event based on the type of command, if there is no
            //  event registered, send this to the client.
            SmtpCommandEvent event;
            if ((event = SmtpCommandEvent.get(command.getType())) == null) {
                throw new SmtpCommand.UnrecognizedException("No event registered for: " + command.getType().toString() + '.');
            }

            // Calls the two required methods, allow and handle... Allow makes
            //  sure that the called command may be called, and handle performs it
            event.getHandler().allowed(client, session, command);
            event.getHandler().handle(client, session, command);
        } catch (SmtpCommand.SyntaxException e) {
            new SmtpReply(501, SmtpReply.EnhancedStatusCode.detSyntaxError.add(SmtpReply.EnhancedStatusCode.classPermanentFailure),
                    e.getMessage()).write(client);
            client.close();
        } catch (SmtpCommand.UnrecognizedException e) {
            new SmtpReply(502, SmtpReply.EnhancedStatusCode.detInvalidCommand.add(SmtpReply.EnhancedStatusCode.classPermanentFailure),
                    e.getMessage()).write(client);
        } catch (SmtpCommand.SequenceException e) {
            new SmtpReply(503, SmtpReply.EnhancedStatusCode.detInvalidCommand.add(SmtpReply.EnhancedStatusCode.classPermanentFailure),
                    e.getMessage()).write(client);
            client.close();
        } catch (SmtpCommand.InvalidArgumentException e) {
            new SmtpReply(501, SmtpReply.EnhancedStatusCode.detInvalidArguments.add(SmtpReply.EnhancedStatusCode.classPermanentFailure),
                    e.getMessage()).write(client);
            client.close();
        } catch (SmtpCommand.AuthenticationRequiredException e) {
            new SmtpReply(530, SmtpReply.EnhancedStatusCode.classPermanentFailure.add(SmtpReply.EnhancedStatusCode.subSecurityOrPolicyStatus),
                    "Authentication required: " + e.getMessage()).write(client);
            client.close();
        } catch (Exception e) {
            new SmtpReply(500, SmtpReply.EnhancedStatusCode.detOtherUndefinedStatus.add(SmtpReply.EnhancedStatusCode.classPermanentFailure),
                    e.getMessage()).write(client);
        }
    }
}
