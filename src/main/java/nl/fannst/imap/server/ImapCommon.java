/*(
    Copyright Luke A.C.A. Rieff 2020 - All Rights Reserved
)*/

package nl.fannst.imap.server;

import nl.fannst.datatypes.SegmentedBuffer;
import nl.fannst.net.NIOClientWrapper;
import nl.fannst.net.NIOClientWrapperArgument;

public class ImapCommon {
    private static void onCommand(NIOClientWrapperArgument client) {
        NIOClientWrapper clientWrapper = client.getClientWrapper();
        SegmentedBuffer buffer = clientWrapper.getSegmentedBuffer();

        String line = buffer.read();
        System.out.println(line);
    }

    public static void onData(NIOClientWrapperArgument client) {
        NIOClientWrapper clientWrapper = client.getClientWrapper();
        SegmentedBuffer buffer = clientWrapper.getSegmentedBuffer();

        boolean more = false;
        do {
            if (buffer.lineAvailable()) {
                onCommand(client);

                more = buffer.lineAvailable();
            }
        } while (more);

        buffer.shift();
    }
}
