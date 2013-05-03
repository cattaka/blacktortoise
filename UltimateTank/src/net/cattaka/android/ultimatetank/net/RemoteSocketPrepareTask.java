
package net.cattaka.android.ultimatetank.net;

import java.io.IOException;
import java.net.Socket;

import net.cattaka.android.ultimatetank.Constants;
import net.cattaka.libgeppa.IRawSocket;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;
import android.content.Context;
import android.util.Log;

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
        Socket socket = null;
        IRawSocket rawSocket = null;
        try {
            socket = new Socket(mHostname, port);
            rawSocket = new RemoteSocket(socket);
        } catch (IOException e) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e2) {
                    Log.w(Constants.TAG, e2.getMessage(), e);
                }
            }
        }
        return rawSocket;
    }

}
