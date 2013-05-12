
package net.blacktortoise.android;

import java.util.Map;

import net.blacktortoise.android.common.IDeviceAdapterListener;
import net.blacktortoise.android.common.adapter.BtConnectionAdapter;
import net.blacktortoise.android.common.adapter.LocalDeviceAdapter;
import net.blacktortoise.android.common.data.BtPacket;
import net.blacktortoise.android.common.data.DeviceEventCode;
import net.blacktortoise.android.common.data.DeviceState;
import net.blacktortoise.android.common.data.OpCode;
import net.blacktortoise.android.util.AidlUtil;
import net.blacktortoise.android.util.AidlUtil.CallFunction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.SparseArray;

public class BlackTortoiseService extends Service {
    protected static final String ACTION_USB_PERMISSION = "net.cattaka.android.blacktortoise.action_permission";

    protected static final String EXTRA_USB_DEVICE_KEY = "usbDevicekey";

    private static final int NOTIFICATION_CONNECTED_ID = 1;

    private static final int EVENT_REGISTER_CONNECTION_LISTENER = 1;

    private static final int EVENT_UNREGISTER_CONNECTION_LISTENER = 2;

    private static final int EVENT_CONNECT = 3;

    private static final int EVENT_DISCONNECT = 4;

    private static final int EVENT_REQUEST_CAMERA_IMAGE = 5;

    private static final int EVENT_SEND_PACKET = 6;

    private static final int EVENT_SEND_MOVE = 7;

    private static final int EVENT_SEND_HEAD = 8;

    private static Handler sHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Object objs[] = (Object[])msg.obj;
            BlackTortoiseService target = (BlackTortoiseService)objs[0];
            BtConnectionAdapter mcThread = target.mConnectionThread;
            switch (msg.what) {
                case EVENT_REGISTER_CONNECTION_LISTENER: {
                    target.mServiceListeners.append((Integer)objs[1],
                            (IBlackTortoiseServiceListener)objs[2]);
                    break;
                }
                case EVENT_UNREGISTER_CONNECTION_LISTENER: {
                    target.mServiceListeners.remove((Integer)objs[1]);
                    break;
                }
                case EVENT_CONNECT: {
                    target.connect((String)objs[1]);
                    break;
                }
                case EVENT_DISCONNECT: {
                    target.disconnect();
                    break;
                }

                default: {
                    if (mcThread != null) {
                        switch (msg.what) {
                            case EVENT_REQUEST_CAMERA_IMAGE: {
                                mcThread.sendRequestCameraImage();
                                break;
                            }
                            case EVENT_SEND_PACKET: {
                                mcThread.sendPacket((BtPacket)objs[1]);
                                break;
                            }
                            case EVENT_SEND_MOVE: {
                                float[] vs = (float[])objs[1];
                                mcThread.sendMove(vs[0], vs[1], vs[2], vs[3]);
                                break;
                            }
                            case EVENT_SEND_HEAD: {
                                float[] vs = (float[])objs[1];
                                mcThread.sendHead(vs[0], vs[1]);
                                break;
                            }
                        }
                    }
                    if (mcThread != null) {
                        switch (msg.what) {
                            case EVENT_REQUEST_CAMERA_IMAGE: {
                                mcThread.sendRequestCameraImage();
                                break;
                            }
                            case EVENT_SEND_PACKET: {
                                mcThread.sendPacket((BtPacket)objs[1]);
                                break;
                            }
                            case EVENT_SEND_MOVE: {
                                float[] vs = (float[])objs[1];
                                mcThread.sendMove(vs[0], vs[1], vs[2], vs[3]);
                                break;
                            }
                            case EVENT_SEND_HEAD: {
                                float[] vs = (float[])objs[1];
                                mcThread.sendHead(vs[0], vs[1]);
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        };
    };

    private BlackTortoiseService me = this;

    private LocalDeviceAdapter mConnectionThread;

    private int mNextConnectionListenerSeq = 1;

    private SparseArray<IBlackTortoiseServiceListener> mServiceListeners;

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                String itemKey = intent.getStringExtra(EXTRA_USB_DEVICE_KEY);
                if (itemKey != null) {
                    connect(itemKey);
                }
            }
        }
    };

    private IBinder mBinder = new IBlackTortoiseService.Stub() {
        @Override
        public int registerServiceListener(IBlackTortoiseServiceListener listener)
                throws RemoteException {
            int seq = mNextConnectionListenerSeq++;
            sHandler.obtainMessage(EVENT_REGISTER_CONNECTION_LISTENER, new Object[] {
                    me, seq, listener
            }).sendToTarget();
            return seq;
        }

        @Override
        public void unregisterServiceServiceListener(int seq) throws RemoteException {
            sHandler.obtainMessage(EVENT_UNREGISTER_CONNECTION_LISTENER, new Object[] {
                    me, seq
            }).sendToTarget();
        }

        public void connect(String deviceKey) throws RemoteException {
            sHandler.obtainMessage(EVENT_CONNECT, new Object[] {
                    me, deviceKey
            }).sendToTarget();
        };

        public void disconnect() throws RemoteException {
            sHandler.obtainMessage(EVENT_DISCONNECT, new Object[] {
                me
            }).sendToTarget();
        };

        @Override
        public boolean requestCameraImage() throws RemoteException {
            if (mConnectionThread != null) {
                sHandler.obtainMessage(EVENT_REQUEST_CAMERA_IMAGE, new Object[] {
                    me
                }).sendToTarget();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean sendPacket(BtPacket packet) throws RemoteException {
            if (mConnectionThread != null) {
                sHandler.obtainMessage(EVENT_SEND_PACKET, new Object[] {
                        me, packet
                }).sendToTarget();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean sendMove(float leftMotor1, float leftMotor2, float rightMotor1,
                float rightMotor2) throws RemoteException {
            if (mConnectionThread != null) {
                sHandler.obtainMessage(EVENT_SEND_MOVE, new Object[] {
                        me, new float[] {
                                leftMotor1, leftMotor2, rightMotor1, rightMotor2
                        }
                }).sendToTarget();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean sendHead(float yaw, float pitch) throws RemoteException {
            if (mConnectionThread != null) {
                sHandler.obtainMessage(EVENT_SEND_HEAD, new Object[] {
                        me, new float[] {
                                yaw, pitch
                        }
                }).sendToTarget();
                return true;
            } else {
                return false;
            }
        }
    };

    private IDeviceAdapterListener mDeviceAdapterListener = new IDeviceAdapterListener() {
        @Override
        public void onReceiveCameraImage(int cameraIdx, Bitmap bitmat) {
            // Not used.
        }

        public void onReceiveEcho(byte[] data) {
            final BtPacket packet = new BtPacket(OpCode.ECHO, data.length, data);
            AidlUtil.callMethods(mServiceListeners,
                    new CallFunction<IBlackTortoiseServiceListener>() {
                        public boolean run(IBlackTortoiseServiceListener item)
                                throws RemoteException {
                            item.onReceive(packet);
                            return true;
                        };
                    });
        };

        @Override
        public void onDeviceStateChanged(final DeviceState state, final DeviceEventCode code) {
            AidlUtil.callMethods(mServiceListeners,
                    new CallFunction<IBlackTortoiseServiceListener>() {
                        public boolean run(IBlackTortoiseServiceListener item)
                                throws RemoteException {
                            item.onDeviceStateChanged(state, code);
                            return true;
                        };
                    });
            handleConnectedNotification(state == DeviceState.CONNECTED);
        }
    };

    public BlackTortoiseService() {
        mServiceListeners = new SparseArray<IBlackTortoiseServiceListener>();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }

    private void connect(String deviceKey) {
        disconnect();

        UsbManager usbManager = (UsbManager)getSystemService(USB_SERVICE);
        Map<String, UsbDevice> devices = usbManager.getDeviceList();
        UsbDevice usbDevice = devices.get(deviceKey);
        if (usbManager.hasPermission(usbDevice)) {
            // If service already has permission, it start thread.
            mConnectionThread = new LocalDeviceAdapter(mDeviceAdapterListener, true, usbManager,
                    usbDevice);
            try {
                mConnectionThread.startAdapter();
            } catch (InterruptedException e) {
                // Impossible
                throw new RuntimeException(e);
            }
        } else {
            // Request
            Intent intent = new Intent(ACTION_USB_PERMISSION);
            intent.putExtra(EXTRA_USB_DEVICE_KEY, deviceKey);
            PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            usbManager.requestPermission(usbDevice, pIntent);
        }
    }

    private void disconnect() {
        if (mConnectionThread != null) {
            try {
                mConnectionThread.stopAdapter();
            } catch (InterruptedException e) {
                // Impossible
                throw new RuntimeException(e);
            }
        }
    }

    private void handleConnectedNotification(boolean connected) {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (connected) {
            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentTitle(getText(R.string.notification_title_connected));
            builder.setSmallIcon(R.drawable.ic_launcher);
            @SuppressWarnings("deprecation")
            Notification nortification = builder.getNotification();
            manager.notify(NOTIFICATION_CONNECTED_ID, nortification);
        } else {
            manager.cancel(NOTIFICATION_CONNECTED_ID);
        }
    }
}
