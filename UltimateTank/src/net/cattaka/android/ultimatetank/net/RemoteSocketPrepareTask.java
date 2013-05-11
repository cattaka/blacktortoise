
package net.cattaka.android.ultimatetank.net;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.cattaka.libgeppa.IRawSocket;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;
import android.content.Context;

public class RemoteSocketPrepareTask implements IRawSocketPrepareTask {
    private String mHostname;

    private int port;

    public RemoteSocketPrepareTask(String hostname, int port) {
        super();
        mHostname = hostname;
        this.port = port;
    }

    @Override
    public void setup(Context context) {
        // none
    }

    @Override
    public IRawSocket prepareRawSocket() {
        IRawSocket rawSocket = null;
        SocketAddress remoteAddr = new InetSocketAddress(mHostname, port);
        rawSocket = new RemoteSocket(remoteAddr);
        return rawSocket;
    }
}
