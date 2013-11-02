
package net.blacktortoise.android.remocon;

import net.blacktortoise.android.remocon.util.NormalizedOnTouchListener;
import net.blacktortoise.androidlib.BlackTortoiseFunctions;
import net.blacktortoise.androidlib.BlackTortoiseServiceWrapper;
import net.blacktortoise.androidlib.IBlackTortoiseService;
import net.blacktortoise.androidlib.IBlackTortoiseServiceListener;
import net.blacktortoise.androidlib.IDeviceAdapterListener;
import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.DeviceEventCode;
import net.blacktortoise.androidlib.data.DeviceInfo;
import net.blacktortoise.androidlib.data.DeviceState;
import net.blacktortoise.androidlib.data.OpCode;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements IDeviceAdapterListener, OnClickListener {
    private static final int EVENT_ON_DEVICE_STATE_CHANGED = 1;

    private static final int EVENT_ON_RECEIVE_PACKET = 2;

    private MainActivity me = this;

    private BlackTortoiseServiceWrapper mServiceWrapper;

    private int mServiceListenerSeq = -1;

    private ImageView mCameraImage;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceWrapper = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            IBlackTortoiseService service = IBlackTortoiseService.Stub.asInterface(binder);
            mServiceWrapper = new BlackTortoiseServiceWrapper(service);
            if (mServiceListenerSeq < 0) {
                try {
                    mServiceListenerSeq = mServiceWrapper.getService().registerServiceListener(
                            mServiceListener);
                } catch (RemoteException e) {
                    // Nothing to do
                    throw new RuntimeException(e);
                }
            }
        }
    };

    private IBlackTortoiseServiceListener mServiceListener = new IBlackTortoiseServiceListener.Stub() {
        @Override
        public void onReceivePacket(BtPacket packet) throws RemoteException {
            sHandler.obtainMessage(EVENT_ON_RECEIVE_PACKET, new Object[] {
                    me, packet
            }).sendToTarget();
        }

        public void onDeviceStateChanged(final DeviceState state, final DeviceEventCode code,
                final DeviceInfo deviceInfo) throws RemoteException {
            sHandler.obtainMessage(EVENT_ON_DEVICE_STATE_CHANGED, new Object[] {
                    me, state, code, deviceInfo
            }).sendToTarget();
        }
    };

    private static Handler sHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Object[] objs = (Object[])msg.obj;
            MainActivity target = (MainActivity)objs[0];
            if (msg.what == EVENT_ON_RECEIVE_PACKET) {
                target.onReceivePacket((BtPacket)objs[1]);
            } else if (msg.what == EVENT_ON_DEVICE_STATE_CHANGED) {
                target.onDeviceStateChanged((DeviceState)objs[1], (DeviceEventCode)objs[2],
                        (DeviceInfo)objs[3]);
            }
        };
    };

    private OnTouchListener mOnTouchListener = new NormalizedOnTouchListener() {
        long lastSendHeadTime;

        long lastSendMoveTime;

        @Override
        public boolean onTouch(View v, MotionEvent event, float rx, float ry) {
            if (v.getId() == R.id.controller_head) {
                long t = SystemClock.elapsedRealtime();
                if (t - lastSendHeadTime > 100 || event.getActionMasked() == MotionEvent.ACTION_UP) {
                    float yaw = (1 - rx);
                    float pitch = (1 - ry);
                    { // Sends command
                        BlackTortoiseServiceWrapper wrapper = mServiceWrapper;
                        if (wrapper != null) {
                            wrapper.sendHead(yaw, pitch);
                        }
                    }
                    lastSendHeadTime = t;
                }
            } else if (v.getId() == R.id.controller_move) {
                long t = SystemClock.elapsedRealtime();
                if (t - lastSendMoveTime > 100 || event.getActionMasked() == MotionEvent.ACTION_UP) {
                    float forward = -(ry * 2 - 1);
                    float turn = rx * 2 - 1;
                    if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                        forward = 0f;
                        turn = 0f;
                    }
                    { // Sends command
                        BlackTortoiseServiceWrapper wrapper = mServiceWrapper;
                        if (wrapper != null) {
                            wrapper.sendMove(forward, turn);
                        }
                    }
                    lastSendMoveTime = t;
                }
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // Pickup views
        mCameraImage = (ImageView)findViewById(R.id.camera_image);

        // Binds event listeners
        findViewById(R.id.controller_head).setOnTouchListener(mOnTouchListener);
        findViewById(R.id.controller_move).setOnTouchListener(mOnTouchListener);
        findViewById(R.id.selectDeviceButton).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent service = BlackTortoiseFunctions.createServiceIntent();
        startService(service);
        bindService(service, mServiceConnection, 0);
        updateState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceListenerSeq >= 0) {
            try {
                mServiceWrapper.getService().unregisterServiceListener(mServiceListenerSeq);
                mServiceListenerSeq = -1;
            } catch (RemoteException e) {
                // Nothing to do
                throw new RuntimeException(e);
            }
        }
        unbindService(mServiceConnection);
    }

    @Override
    public void onDeviceStateChanged(DeviceState state, DeviceEventCode code, DeviceInfo deviceInfo) {
        if (state == DeviceState.CONNECTED) {
            mServiceWrapper.sendRequestCameraImage(0);
        }
        updateState();
    }

    @Override
    public void onReceivePacket(BtPacket packet) {
        if (packet.getOpCode() == OpCode.CAMERA_IMAGE) {
            // int cameraIdx = packet.getData()[0];
            Bitmap bitmap = BitmapFactory.decodeByteArray(packet.getData(), 1,
                    packet.getDataLen() - 1);
            if (bitmap != null) {
                mCameraImage.setImageBitmap(bitmap);
            }
            mServiceWrapper.sendRequestCameraImage(0);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.selectDeviceButton) {
            Intent intent = BlackTortoiseFunctions.createSelectDeviceActivityIntent();
            startActivity(intent);
        }
    }

    private void updateState() {
        TextView serviceStateText = (TextView)findViewById(R.id.serviceStateText);
        Button selectDeviceButton = (Button)findViewById(R.id.selectDeviceButton);
        if (mServiceWrapper == null) {
            serviceStateText.setVisibility(View.VISIBLE);
            serviceStateText.setText(R.string.status_no_service);
            selectDeviceButton.setVisibility(View.INVISIBLE);
            mCameraImage.setVisibility(View.INVISIBLE);
        } else if (mServiceWrapper.getCurrentDeviceInfo() == null) {
            serviceStateText.setVisibility(View.VISIBLE);
            serviceStateText.setText(R.string.status_no_device);
            selectDeviceButton.setVisibility(View.VISIBLE);
            mCameraImage.setVisibility(View.INVISIBLE);
        } else {
            serviceStateText.setVisibility(View.INVISIBLE);
            selectDeviceButton.setVisibility(View.VISIBLE);
            mCameraImage.setVisibility(View.VISIBLE);
        }

    }
}
