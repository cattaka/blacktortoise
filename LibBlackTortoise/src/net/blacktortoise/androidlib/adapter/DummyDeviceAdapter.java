
package net.blacktortoise.androidlib.adapter;

import net.blacktortoise.androidlib.IDeviceAdapter;
import net.blacktortoise.androidlib.IDeviceAdapterListener;
import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.DeviceEventCode;
import net.blacktortoise.androidlib.data.DeviceInfo;
import net.blacktortoise.androidlib.data.DeviceState;
import net.blacktortoise.androidlib.data.OpCode;
import android.os.Handler;

public class DummyDeviceAdapter implements IDeviceAdapter {
    private IDeviceAdapterListener mListener;

    private DeviceState mLastDeviceState = DeviceState.INITIAL;

    private Handler mHandler;

    public DummyDeviceAdapter(IDeviceAdapterListener listener, Handler handler) {
        mListener = listener;
        mHandler = handler;
    }

    @Override
    public void startAdapter() throws InterruptedException {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                DeviceInfo deviceInfo = getDeviceInfo();
                mListener.onDeviceStateChanged(DeviceState.CONNECTING, DeviceEventCode.UNKNOWN,
                        deviceInfo);
                mListener.onDeviceStateChanged(DeviceState.CONNECTED, DeviceEventCode.UNKNOWN,
                        deviceInfo);
                mLastDeviceState = DeviceState.CONNECTED;
            }
        });
    }

    @Override
    public void stopAdapter() throws InterruptedException {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                DeviceInfo deviceInfo = getDeviceInfo();
                mListener.onDeviceStateChanged(DeviceState.CLOSED, DeviceEventCode.DISCONNECTED,
                        deviceInfo);
                mLastDeviceState = DeviceState.CLOSED;
            }
        });
    }

    @Override
    public DeviceState getDeviceState() {
        return mLastDeviceState;
    }

    @Override
    public boolean sendPacket(BtPacket packet) {
        if (packet.getOpCode() == OpCode.ECHO) {
            byte[] data = new byte[packet.getDataLen()];
            System.arraycopy(packet.getData(), 0, data, 0, data.length);
            final BtPacket respPacket = new BtPacket(OpCode.ECHO, data.length, data);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onReceivePacket(respPacket);
                }
            });
        }
        return true;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return DeviceInfo.createDummy(false);
    }

    @Override
    public boolean isCameraSupported() {
        return false;
    }
}
