
package net.blacktortoise.androidlib.adapter;

import net.blacktortoise.androidlib.IDeviceAdapter;
import net.blacktortoise.androidlib.IDeviceAdapterListener;
import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.BtPacketFactory;
import net.blacktortoise.androidlib.data.DeviceEventCode;
import net.blacktortoise.androidlib.data.DeviceInfo;
import net.blacktortoise.androidlib.data.DeviceState;
import net.blacktortoise.androidlib.data.OpCode;
import net.blacktortoise.androidlib.util.DeviceUtil;
import net.cattaka.libgeppa.data.ConnectionCode;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.thread.ConnectionThread;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;
import net.cattaka.libgeppa.thread.IConnectionThreadListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public abstract class BtConnectionAdapter implements IDeviceAdapter {
    private byte[] mBuffer = new byte[0x100];

    private ConnectionThread<BtPacket> mConnectionThread;

    private IDeviceAdapterListener mListener;

    private BtPacketFactory mPacketFactory;
    {
        mPacketFactory = new BtPacketFactory();
    }

    private IConnectionThreadListener<BtPacket> mConnectionThreadListener = new IConnectionThreadListener<BtPacket>() {

        @Override
        public void onReceive(BtPacket packet) {
            if (packet.getOpCode() == OpCode.CAMERA_IMAGE) {
                int cameraIdx = packet.getData()[0];
                Bitmap bitmap = BitmapFactory.decodeByteArray(packet.getData(), 1,
                        packet.getDataLen() - 1);
                if (bitmap != null) {
                    mListener.onReceiveCameraImage(cameraIdx, bitmap);
                }
            } else {
                mListener.onReceive(packet);
            }
        }

        @Override
        public void onConnectionStateChanged(ConnectionState state, ConnectionCode code) {
            DeviceState dState = DeviceUtil.toDeviceState(state);
            DeviceEventCode deCode = DeviceUtil.toDeviceEventCode(code);
            DeviceInfo deviceInfo = getDeviceInfo();
            mListener.onDeviceStateChanged(dState, deCode, deviceInfo);
        }
    };

    public BtConnectionAdapter(IDeviceAdapterListener listener, boolean useMainLooperForListener) {
        super();
        mListener = listener;
    }

    abstract protected IRawSocketPrepareTask createRawSocketPrepareTask();

    @Override
    public void startAdapter() throws InterruptedException {
        if (mConnectionThread != null) {
            throw new IllegalStateException("Already running.");
        }
        mConnectionThread = new ConnectionThread<BtPacket>(createRawSocketPrepareTask(),
                mPacketFactory, mConnectionThreadListener, true);
        mConnectionThread.startThread();
    }

    @Override
    public void stopAdapter() throws InterruptedException {
        mConnectionThread.stopThread();
    }

    @Override
    public boolean sendPacket(BtPacket packet) {
        return mConnectionThread.sendPacket(packet);
    }

    @Override
    public boolean sendRequestCameraImage() {
        BtPacket packet = new BtPacket(OpCode.REQUEST_CAMERA_IMAGE, 0, mBuffer);
        return sendPacket(packet);
    }

    @Override
    public DeviceState getDeviceState() {
        if (mConnectionThread == null) {
            return DeviceState.UNKNOWN;
        }
        return DeviceUtil.toDeviceState(mConnectionThread.getLastConnectionState());
    }

}
