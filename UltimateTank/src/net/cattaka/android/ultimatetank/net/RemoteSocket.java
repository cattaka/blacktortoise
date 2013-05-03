
package net.cattaka.android.ultimatetank.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.cattaka.libgeppa.IRawSocket;

public class RemoteSocket implements IRawSocket {
    private Socket mSocket;

    private InputStream mInputStream;

    private OutputStream mOutputStream;

    public RemoteSocket(Socket socket) throws IOException {
        super();
        mSocket = socket;
        mInputStream = mSocket.getInputStream();

        mOutputStream = mSocket.getOutputStream();
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
