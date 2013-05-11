
package net.blacktortoise.android.common.adapter;

import net.blacktortoise.android.BlackTortoiseService;
import net.blacktortoise.android.IBlackTortoiseService;
import net.blacktortoise.android.IBlackTortoiseServiceListener;
import net.blacktortoise.android.common.IDeviceAdapter;
import net.blacktortoise.android.common.IDeviceAdapterListener;
import net.blacktortoise.android.common.data.BtPacket;
import net.blacktortoise.android.common.data.DeviceEventCode;
import net.blacktortoise.android.common.data.DeviceState;
import net.blacktortoise.android.common.data.OpCode;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

public class BtServiceAdapter implements IDeviceAdapter {
    private static final int EVENT_ON_DEVICE_STATE_CHANGED = 1;

    private static final int EVENT_ON_RECEIVE_CAMER_IMAGE = 2;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // none
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mService = IBlackTortoiseService.Stub.asInterface(binder);
            try {
                mService.connect(mDeviceId);
                mService.registerServiceListener(mServiceListener);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private IBlackTortoiseServiceListener mServiceListener = new IBlackTortoiseServiceListener.Stub() {
        public void onDeviceStateChanged(DeviceState state, DeviceEventCode code)
                throws RemoteException {
            sHandler.obtainMessage(EVENT_ON_DEVICE_STATE_CHANGED, new Object[] {
                    me, state, code
            }).sendToTarget();
        }

        public void onReceive(BtPacket packet) throws RemoteException {
            if (packet.getOpCode() == OpCode.CAMERA_IMAGE) {
                int cameraIdx = packet.getData()[0];
                Bitmap bitmap = BitmapFactory.decodeByteArray(packet.getData(), 1,
                        packet.getDataLen() - 1);
                if (bitmap != null) {
                    sHandler.obtainMessage(EVENT_ON_RECEIVE_CAMER_IMAGE, new Object[] {
                            me, cameraIdx, bitmap
                    }).sendToTarget();
                }
            } else {
                // Currently, There are no other events.
            }
        }
    };

    private static Handler sHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Object[] objs = (Object[])msg.obj;
            BtServiceAdapter target = (BtServiceAdapter)objs[0];
            if (msg.what == EVENT_ON_RECEIVE_CAMER_IMAGE) {
                target.mListener.onReceiveCameraImage((Integer)objs[1], (Bitmap)objs[2]);
            } else if (msg.what == EVENT_ON_DEVICE_STATE_CHANGED) {
                target.mListener.onDeviceStateChanged((DeviceState)objs[1],
                        (DeviceEventCode)objs[2]);
            }
        };
    };

    private BtServiceAdapter me = this;

    private IBlackTortoiseService mService;

    private Context mContext;

    private String mDeviceId;

    private DeviceState mLastDeviceState = DeviceState.INITIAL;

    private IDeviceAdapterListener mListener;

    @Override
    public void startAdapter() throws InterruptedException {
        Intent intent = new Intent(mContext, BlackTortoiseService.class);
        mContext.bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    public BtServiceAdapter(IDeviceAdapterListener listener, Context context, String deviceId) {
        super();
        mListener = listener;
        mContext = context;
        mDeviceId = deviceId;
    }

    @Override
    public void stopAdapter() throws InterruptedException {
        mContext.unbindService(mServiceConnection);
    }

    @Override
    public boolean sendPacket(BtPacket packet) {
        if (mService != null) {
            try {
                return mService.sendPacket(packet);
            } catch (RemoteException e) {
                // ignore
            }
        }
        return false;
    }

    @Override
    public boolean sendMove(float leftMotor1, float leftMotor2, float rightMotor1, float rightMotor2) {
        if (mService != null) {
            try {
                return mService.sendMove(leftMotor1, leftMotor2, rightMotor1, rightMotor2);
            } catch (RemoteException e) {
                // ignore
            }
        }
        return false;
    }

    @Override
    public boolean sendHead(float yaw, float pitch) {
        if (mService != null) {
            try {
                return mService.sendHead(yaw, pitch);
            } catch (RemoteException e) {
                // ignore
            }
        }
        return false;
    }

    @Override
    public boolean sendRequestCameraImage() {
        // Service do not support the camera.
        return false;
    }

    @Override
    public DeviceState getDeviceState() {
        return mLastDeviceState;
    }

}
