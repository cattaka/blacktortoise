
package net.cattaka.android.ultimatetank.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import net.cattaka.android.ultimatetank.Constants;
import net.cattaka.libgeppa.IRawSocket;
import android.util.Log;

public class RemoteSocket implements IRawSocket {
    private SocketAddress mSocketAddress;

    private Socket mSocket;

    private InputStream mInputStream;

    private OutputStream mOutputStream;

    public RemoteSocket(SocketAddress socketAddress) {
        super();
        mSocket = new Socket();
        mSocketAddress = socketAddress;
    }

    public boolean setup() {
        try {
            mSocket.connect(mSocketAddress);
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            try {
                mSocket.close();
            } catch (IOException e2) {
                // Impossible
                Log.w(Constants.TAG, e2.getMessage(), e2);
            }
            return false;
        }
    }

    @Override
    public String getLabel() {
        return "Remote:" + mSocket.getRemoteSocketAddress().toString() + ":" + mSocket.getPort();
    }

    @Override
    public InputStream getInputStream() {
        return mInputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    @Override
    public void close() throws IOException {
        mSocket.close();
    }

    @Override
    public boolean isConnected() {
        return mSocket.isConnected();
    }

}
