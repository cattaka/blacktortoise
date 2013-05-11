
package net.blacktortoise.android.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.blacktortoise.android.Constants;
import net.blacktortoise.android.common.data.BtPacket;
import net.blacktortoise.android.net.ClientThread.IClientThreadListener;
import net.blacktortoise.android.net.data.SocketState;
import android.os.Handler;
import android.util.Log;

public class ServerThread extends Thread {
    private static final int EVENT_ON_SOCKET_STATE_CHANGED = 1;

    private static final int EVENT_ON_CLIENT_CONNECTED = 2;

    private static final int EVENT_ON_CLIENT_DISCONNECTED = 3;

    private static final int EVENT_ON_RECEIVE_PACKET = 4;

    private static final int EVENT_CLEAN_UP = 5;

    public interface IServerThreadListener {
        public void onClientConnected(ClientThread target);

        public void onClientDisconnected(ClientThread target);

        public void onSocketStateChanged(SocketState socketState);

        public void onReceivePacket(ClientThread from, BtPacket packet);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == EVENT_ON_SOCKET_STATE_CHANGED) {
                mListener.onSocketStateChanged((SocketState)msg.obj);
            } else if (msg.what == EVENT_ON_CLIENT_CONNECTED) {
                ClientThread target = (ClientThread)msg.obj;
                mClientThreads.add(target);
                mListener.onClientConnected(target);
            } else if (msg.what == EVENT_ON_CLIENT_DISCONNECTED) {
                ClientThread target = (ClientThread)msg.obj;
                mClientThreads.remove((ClientThread)msg.obj);
                mListener.onClientDisconnected(target);
            } else if (msg.what == EVENT_ON_RECEIVE_PACKET) {
                Object[] objs = (Object[])msg.obj;
                mListener.onReceivePacket((ClientThread)objs[0], (BtPacket)objs[1]);
            } else if (msg.what == EVENT_CLEAN_UP) {
                for (ClientThread ct : mClientThreads) {
                    ct.stopThread();
                }
            }
        };
    };

    private IClientThreadListener mClientThreadListener = new IClientThreadListener() {

        @Override
        public void onReceivePacket(ClientThread target, BtPacket packet) {
            mHandler.obtainMessage(EVENT_ON_RECEIVE_PACKET, new Object[] {
                    target, packet
            }).sendToTarget();
        }

        @Override
        public void onDisconnected(ClientThread target) {
            mHandler.obtainMessage(EVENT_ON_CLIENT_DISCONNECTED, target).sendToTarget();
        }
    };

    private ServerSocket mServerSocket;

    private int mPort;

    private IServerThreadListener mListener;

    private List<ClientThread> mClientThreads;

    private CountDownLatch mStartLatch;

    public ServerThread(int port, IServerThreadListener listener) {
        super("ServerThread:" + port);
        this.mPort = port;
        this.mListener = listener;
        mClientThreads = new LinkedList<ClientThread>();
    }

    @Override
    public void run() {
        try {
            { // Creating SeverSocket
                mHandler.obtainMessage(EVENT_ON_SOCKET_STATE_CHANGED, SocketState.INIT)
                        .sendToTarget();
                mServerSocket = new ServerSocket(mPort);
                mHandler.obtainMessage(EVENT_ON_SOCKET_STATE_CHANGED, SocketState.OPEN)
                        .sendToTarget();
            }
            mStartLatch.countDown();
            while (true) {
                Socket socket = mServerSocket.accept();
                ClientThread clientThread = new ClientThread(socket, mClientThreadListener);
                clientThread.start();
                mHandler.obtainMessage(EVENT_ON_CLIENT_CONNECTED, clientThread).sendToTarget();
            }
        } catch (IOException e) {
            // none
            Log.d(Constants.TAG, e.getMessage(), e);
        } finally {
            mHandler.obtainMessage(EVENT_CLEAN_UP).sendToTarget();
            mHandler.obtainMessage(EVENT_ON_SOCKET_STATE_CHANGED, SocketState.CLOSE).sendToTarget();
            try {
                if (mServerSocket != null && !mServerSocket.isClosed()) {
                    mServerSocket.close();
                }
            } catch (IOException e) {
                // Impossible
                Log.w(Constants.TAG, e.getMessage(), e);
            }
        }
    }

    public void startThread() throws InterruptedException {
        mStartLatch = new CountDownLatch(1);
        start();
        mStartLatch.await();
    }

    public void stopThread() {
        if (isAlive()) {
            try {
                if (!mServerSocket.isClosed()) {
                    mServerSocket.close();
                }
            } catch (IOException e) {
                // Impossible
                Log.w(Constants.TAG, e.getMessage(), e);
            }
        }
    }

    public List<ClientThread> getClientThreads() {
        return mClientThreads;
    }

    public void sendPacket(BtPacket packet) {
        for (ClientThread ct : mClientThreads) {
            ct.sendPacket(packet);
        }
    }

}
