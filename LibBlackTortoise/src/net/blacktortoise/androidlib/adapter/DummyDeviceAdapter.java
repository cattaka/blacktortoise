
package net.blacktortoise.androidlib.adapter;

import net.blacktortoise.androidlib.IDeviceAdapter;
import net.blacktortoise.androidlib.IDeviceAdapterListener;
import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.DeviceEventCode;
import net.blacktortoise.androidlib.data.DeviceInfo;
import net.blacktortoise.androidlib.data.DeviceState;
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
        return true;
    }

    @Override
    public boolean sendRequestCameraImage() {
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
