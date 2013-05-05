
package net.cattaka.android.ultimatetank.net;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.cattaka.android.ultimatetank.camera.ICameraManager;
import net.cattaka.android.ultimatetank.camera.RemoteCameraManager;
import net.cattaka.android.ultimatetank.usb.IMySocketPrepareTask;
import net.cattaka.libgeppa.IRawSocket;
import android.content.Context;

public class RemoteSocketPrepareTask implements IMySocketPrepareTask {
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

    @Override
    public ICameraManager createCameraManager() {
        return new RemoteCameraManager();
    }
}
