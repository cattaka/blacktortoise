
package net.blacktortoise.android.net;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.cattaka.libgeppa.IRawSocket;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;

public class RemoteSocketPrepareTask implements IRawSocketPrepareTask {
    private String mHostname;

    private int port;

    public RemoteSocketPrepareTask(String hostname, int port) {
        super();
        mHostname = hostname;
        this.port = port;
    }

    @Override
    public IRawSocket prepareRawSocket() {
        IRawSocket rawSocket = null;
        SocketAddress remoteAddr = new InetSocketAddress(mHostname, port);
        rawSocket = new RemoteSocket(remoteAddr);
        return rawSocket;
    }
}
