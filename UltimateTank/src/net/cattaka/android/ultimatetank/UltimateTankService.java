
package net.cattaka.android.ultimatetank;

import java.util.Map;

import net.cattaka.android.ultimatetank.usb.FtDriverSocketPrepareTask;
import net.cattaka.android.ultimatetank.usb.MyConnectionThread;
import net.cattaka.android.ultimatetank.usb.data.MyPacket;
import net.cattaka.android.ultimatetank.usb.data.MyPacketFactory;
import net.cattaka.android.ultimatetank.util.AidlUtil;
import net.cattaka.android.ultimatetank.util.AidlUtil.CallFunction;
import net.cattaka.libgeppa.data.ConnectionCode;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.thread.IConnectionThreadListener;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.SparseArray;

public class UltimateTankService extends Service {
    protected static final String ACTION_USB_PERMISSION = "net.cattaka.android.ultimatetank.fragment.action_permission";

    protected static final String EXTRA_USB_DEVICE_KEY = "usbDevicekey";

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
            UltimateTankService target = (UltimateTankService)objs[0];
            MyConnectionThread mcThread = target.mConnectionThread;
            switch (msg.what) {
                case EVENT_REGISTER_CONNECTION_LISTENER: {
                    target.mConnectionListeners.append((Integer)objs[1],
                            (IConnectionListener)objs[2]);
                    break;
                }
                case EVENT_UNREGISTER_CONNECTION_LISTENER: {
                    target.mConnectionListeners.remove((Integer)objs[1]);
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
                                mcThread.sendPacket((MyPacket)objs[1]);
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
                                mcThread.sendPacket((MyPacket)objs[1]);
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

    private UltimateTankService me = this;

    private MyConnectionThread mConnectionThread;

    private int mNextConnectionListenerSeq = 1;

    private SparseArray<IConnectionListener> mConnectionListeners;

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

    private IBinder mBinder = new IUltimateTankService.Stub() {
        @Override
        public int registerConnectionListener(IConnectionListener listener) throws RemoteException {
            int seq = mNextConnectionListenerSeq++;
            sHandler.obtainMessage(EVENT_REGISTER_CONNECTION_LISTENER, new Object[] {
                    me, seq, listener
            }).sendToTarget();
            return seq;
        }

        @Override
        public void unregisterConnectionListener(int seq) throws RemoteException {
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
        public boolean sendPacket(MyPacket packet) throws RemoteException {
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

    private IConnectionThreadListener<MyPacket> mConnectionThreadListener = new IConnectionThreadListener<MyPacket>() {

        @Override
        public void onReceive(final MyPacket packet) {
            AidlUtil.callMethods(mConnectionListeners, new CallFunction<IConnectionListener>() {
                public boolean run(IConnectionListener item) throws RemoteException {
                    item.onReceive(packet);
                    return true;
                };
            });
        }

        @Override
        public void onConnectionStateChanged(final ConnectionState state, final ConnectionCode code) {
            AidlUtil.callMethods(mConnectionListeners, new CallFunction<IConnectionListener>() {
                public boolean run(IConnectionListener item) throws RemoteException {
                    item.onConnectionStateChanged(state, code);
                    return true;
                };
            });
        }
    };

    public UltimateTankService() {
        mConnectionListeners = new SparseArray<IConnectionListener>();
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
            FtDriverSocketPrepareTask prepareTask = new FtDriverSocketPrepareTask(usbDevice);
            prepareTask.setup(me);
            mConnectionThread = new MyConnectionThread(prepareTask, new MyPacketFactory(),
                    mConnectionThreadListener);
            try {
                mConnectionThread.startThread();
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
                mConnectionThread.stopThread();
            } catch (InterruptedException e) {
                // Impossible
                throw new RuntimeException(e);
            }
        }
    }
}
