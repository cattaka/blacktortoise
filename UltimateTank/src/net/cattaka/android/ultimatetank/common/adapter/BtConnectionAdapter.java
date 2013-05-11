
package net.cattaka.android.ultimatetank.common.adapter;

import net.cattaka.android.ultimatetank.common.IDeviceAdapter;
import net.cattaka.android.ultimatetank.common.IDeviceAdapterListener;
import net.cattaka.android.ultimatetank.common.data.BtPacket;
import net.cattaka.android.ultimatetank.common.data.BtPacketFactory;
import net.cattaka.android.ultimatetank.common.data.DeviceEventCode;
import net.cattaka.android.ultimatetank.common.data.DeviceState;
import net.cattaka.android.ultimatetank.common.data.OpCode;
import net.cattaka.android.ultimatetank.util.DeviceUtil;
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
                // Currently, There are no other events.
            }
        }

        @Override
        public void onConnectionStateChanged(ConnectionState state, ConnectionCode code) {
            DeviceState dState = DeviceUtil.toDeviceState(state);
            DeviceEventCode deCode = DeviceUtil.toDeviceEventCode(code);
            mListener.onDeviceStateChanged(dState, deCode);
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
        mConnectionThread.startThread();
        mConnectionThread = new ConnectionThread<BtPacket>(createRawSocketPrepareTask(),
                mPacketFactory, mConnectionThreadListener, true);
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
    public boolean sendMove(float leftMotor1, float leftMotor2, float rightMotor1, float rightMotor2) {
        mBuffer[0] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * leftMotor1)));
        mBuffer[1] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * leftMotor2)));
        mBuffer[2] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * rightMotor1)));
        mBuffer[3] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * rightMotor2)));
        BtPacket packet = new BtPacket(OpCode.MOVE, 4, mBuffer);
        return sendPacket(packet);
    }

    @Override
    public boolean sendHead(float yaw, float pitch) {
        mBuffer[0] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * yaw)));
        mBuffer[1] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * pitch)));
        BtPacket packet = new BtPacket(OpCode.HEAD, 2, mBuffer);
        return sendPacket(packet);
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
