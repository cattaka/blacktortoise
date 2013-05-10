
package net.cattaka.android.ultimatetank;

import net.cattaka.android.ultimatetank.usb.ICommandAdapter;
import net.cattaka.android.ultimatetank.usb.data.MyPacket;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class ServiceCommandAdapter implements ICommandAdapter {
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // none
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mService = IUltimateTankService.Stub.asInterface(binder);
            try {
                mService.connect(mDeviceId);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private IUltimateTankService mService;

    private Context mContext;

    private String mDeviceId;

    @Override
    public void startThread() throws InterruptedException {
        Intent intent = new Intent(mContext, UltimateTankService.class);
        mContext.bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    public ServiceCommandAdapter(Context context, String deviceId) {
        super();
        mContext = context;
        mDeviceId = deviceId;
    }

    @Override
    public void stopThread() throws InterruptedException {
        mContext.unbindService(mServiceConnection);
    }

    @Override
    public boolean sendPacket(MyPacket packet) {
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
        return false;
    }

}
