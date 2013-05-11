
package net.cattaka.android.ultimatetank.common.adapter;

import net.cattaka.android.ultimatetank.common.IDeviceAdapter;
import net.cattaka.android.ultimatetank.common.IDeviceAdapterListener;
import net.cattaka.android.ultimatetank.common.data.BtPacket;
import net.cattaka.android.ultimatetank.common.data.DeviceEventCode;
import net.cattaka.android.ultimatetank.common.data.DeviceState;
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
                mListener.onDeviceStateChanged(DeviceState.CONNECTING, DeviceEventCode.UNKNOWN);
                mListener.onDeviceStateChanged(DeviceState.CONNECTED, DeviceEventCode.UNKNOWN);
                mLastDeviceState = DeviceState.CONNECTED;
            }
        });
    }

    @Override
    public void stopAdapter() throws InterruptedException {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onDeviceStateChanged(DeviceState.CLOSED, DeviceEventCode.DISCONNECTED);
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
    public boolean sendMove(float leftMotor1, float leftMotor2, float rightMotor1, float rightMotor2) {
        return true;
    }

    @Override
    public boolean sendHead(float yaw, float pitch) {
        return true;
    }

    @Override
    public boolean sendRequestCameraImage() {
        return true;
    }
}
