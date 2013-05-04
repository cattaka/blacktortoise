
package net.cattaka.android.ultimatetank.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.cattaka.android.ultimatetank.Constants;
import net.cattaka.android.ultimatetank.usb.data.MyPacket;
import net.cattaka.android.ultimatetank.usb.data.MyPacketFactory;
import android.util.Log;

public class ClientThread extends Thread {
    public interface IClientThreadListener {
        /**
         * Note : This method is called from ClientReceiveThread, it's not UI
         * thread.
         */
        public void onReceivePacket(ClientThread target, MyPacket packet);

        /**
         * Note : This method is called from this thread, it's not UI thread.
         */
        public void onDisconnected(ClientThread target);
    }

    private static class MyEvent {
        private int eventCode;

        private Object data;

        public MyEvent(int eventCode, Object data) {
            super();
            this.eventCode = eventCode;
            this.data = data;
        }
    }

    private static class ClientReceiveThread extends Thread {
        private ClientThread mParent;

        private InputStream mInputStream;

        private MyPacketFactory mPacketFactory;

        private IClientThreadListener mListener;

        public ClientReceiveThread(ClientThread parent, InputStream inputStream,
                MyPacketFactory packetFactory, IClientThreadListener listener) {
            super("ClientReceiveThread:" + parent);
            mParent = parent;
            mInputStream = inputStream;
            mPacketFactory = packetFactory;
            mListener = listener;
        }

        @Override
        public void run() {
            super.run();
            try {
                while (true) {
                    MyPacket packet = mPacketFactory.readPacket(mInputStream);
                    if (packet != null) {
                        mListener.onReceivePacket(mParent, packet);
                    }
                }
            } catch (IOException e) {
                // ignore
            }
            mParent.mEventQueue.add(new MyEvent(0, null));
        }
    }

    private Socket mSocket;

    private IClientThreadListener mListener;

    private BlockingQueue<MyEvent> mEventQueue;

    public ClientThread(Socket socket, IClientThreadListener listener) {
        super("ClientThread:" + socket);
        mSocket = socket;
        mListener = listener;
        mEventQueue = new LinkedBlockingQueue<ClientThread.MyEvent>();
    }

    @Override
    public void run() {
        super.run();
        ClientReceiveThread receiveThread = null;
        try {
            MyPacketFactory packetFactory = new MyPacketFactory();
            InputStream in = mSocket.getInputStream();
            OutputStream out = mSocket.getOutputStream();
            { // Creates receiving thread
              // Note: The receiving thread will stop on mSocket.close() in
              // finally block.
                receiveThread = new ClientReceiveThread(this, in, packetFactory, mListener);
                receiveThread.start();
            }

            while (true) {
                MyEvent event = mEventQueue.take();
                if (event.eventCode == 0) {
                    break;
                } else if (event.eventCode == 1) {
                    packetFactory.writePacket(out, (MyPacket)event.data);
                    out.flush();
                }
            }
        } catch (InterruptedException e) {
            // Impossible
            Log.w(Constants.TAG, e.getMessage(), e);
        } catch (IOException e) {
            // ignore
        } finally {
            mListener.onDisconnected(this);
            try {
                mSocket.close();
            } catch (IOException e) {
                // Impossible
                Log.w(Constants.TAG, e.getMessage(), e);
            }
            if (receiveThread != null) {
                try {
                    receiveThread.join();
                } catch (InterruptedException e) {
                    // Impossible
                    Log.w(Constants.TAG, e.getMessage(), e);
                }
            }
        }
    }

    public void sendPacket(MyPacket packet) {
        mEventQueue.add(new MyEvent(1, packet));
    }

    public String getLabel() {
        return String.valueOf(mSocket.getRemoteSocketAddress());
    }

    public void stopThread() {
        mEventQueue.add(new MyEvent(0, null));
        try {
            mSocket.close();
        } catch (IOException e) {
            // Impossible
            Log.w(Constants.TAG, e.getMessage(), e);
        }
        try {
            this.join();
        } catch (InterruptedException e) {
            // Impossible
            Log.w(Constants.TAG, e.getMessage(), e);
        }
    }
}
