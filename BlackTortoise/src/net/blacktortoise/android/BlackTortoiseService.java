
package net.blacktortoise.android;

import java.util.Map;

import net.blacktortoise.android.util.AidlUtil;
import net.blacktortoise.android.util.AidlUtil.CallFunction;
import net.blacktortoise.androidlib.IBlackTortoiseService;
import net.blacktortoise.androidlib.IBlackTortoiseServiceListener;
import net.blacktortoise.androidlib.IDeviceAdapter;
import net.blacktortoise.androidlib.IDeviceAdapterListener;
import net.blacktortoise.androidlib.adapter.DummyDeviceAdapter;
import net.blacktortoise.androidlib.adapter.LocalDeviceAdapter;
import net.blacktortoise.androidlib.adapter.RemoteDeviceAdapter;
import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.DeviceEventCode;
import net.blacktortoise.androidlib.data.DeviceInfo;
import net.blacktortoise.androidlib.data.DeviceInfo.DeviceType;
import net.blacktortoise.androidlib.data.DeviceState;
import android.app.Notification;
import android.app.NotificationManager;
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
import android.util.Log;
import android.util.SparseArray;

public class BlackTortoiseService extends Service {
    protected static final String ACTION_USB_PERMISSION = "net.cattaka.android.blacktortoise.action_permission";

    protected static final String EXTRA_USB_DEVICE_KEY = "usbDevicekey";

    private static final int NOTIFICATION_CONNECTED_ID = 1;

    private static final int EVENT_REGISTER_CONNECTION_LISTENER = 1;

    private static final int EVENT_UNREGISTER_CONNECTION_LISTENER = 2;

    private static final int EVENT_CONNECT = 3;

    private static final int EVENT_DISCONNECT = 4;

    private static final int EVENT_SEND_PACKET = 6;

    private static Handler sHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Object objs[] = (Object[])msg.obj;
            BlackTortoiseService target = (BlackTortoiseService)objs[0];
            IDeviceAdapter mcThread = target.mDeviceAdapter;
            switch (msg.what) {
                case EVENT_REGISTER_CONNECTION_LISTENER: {
                    IBlackTortoiseServiceListener listener = (IBlackTortoiseServiceListener)objs[2];
                    try {
                        DeviceState state = (mcThread != null) ? mcThread.getDeviceState() : null;
                        DeviceInfo deviceInfo = pickDeviceInfo(mcThread);
                        listener.onDeviceStateChanged(state, DeviceEventCode.ON_REGISTER,
                                deviceInfo);

                        target.mServiceListeners.append((Integer)objs[1], listener);
                    } catch (RemoteException e) {
                        // Ignore
                        Log.w(Constants.TAG, e.getMessage(), e);
                    }
                    break;
                }
                case EVENT_UNREGISTER_CONNECTION_LISTENER: {
                    target.mServiceListeners.remove((Integer)objs[1]);
                    break;
                }
                case EVENT_CONNECT: {
                    target.connect((DeviceInfo)objs[1]);
                    break;
                }
                case EVENT_DISCONNECT: {
                    target.disconnect();
                    break;
                }

                default: {
                    if (mcThread != null) {
                        switch (msg.what) {
                            case EVENT_SEND_PACKET: {
                                mcThread.sendPacket((BtPacket)objs[1]);
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

    private IDeviceAdapter mDeviceAdapter;

    private int mNextConnectionListenerSeq = 1;

    private SparseArray<IBlackTortoiseServiceListener> mServiceListeners;

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                String itemKey = intent.getStringExtra(EXTRA_USB_DEVICE_KEY);
                if (itemKey != null) {
                    DeviceInfo deviceInfo = DeviceInfo.createUsb(itemKey, false);
                    connect(deviceInfo);
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
        public void unregisterServiceListener(int seq) throws RemoteException {
            sHandler.obtainMessage(EVENT_UNREGISTER_CONNECTION_LISTENER, new Object[] {
                    me, seq
            }).sendToTarget();
        }

        public void connect(DeviceInfo deviceInfo) throws RemoteException {
            sHandler.obtainMessage(EVENT_CONNECT, new Object[] {
                    me, deviceInfo
            }).sendToTarget();
        };

        public void disconnect() throws RemoteException {
            sHandler.obtainMessage(EVENT_DISCONNECT, new Object[] {
                me
            }).sendToTarget();
        };

        @Override
        public DeviceInfo getCurrentDeviceInfo() throws RemoteException {
            return me.getCurrentDeviceInfo();
        }

        @Override
        public boolean sendPacket(BtPacket packet) throws RemoteException {
            if (mDeviceAdapter != null) {
                sHandler.obtainMessage(EVENT_SEND_PACKET, new Object[] {
                        me, packet
                }).sendToTarget();
                return true;
            } else {
                return false;
            }
        }
    };

    private IDeviceAdapterListener mDeviceAdapterListener = new IDeviceAdapterListener() {
        public void onReceivePacket(final BtPacket packet) {
            AidlUtil.callMethods(mServiceListeners,
                    new CallFunction<IBlackTortoiseServiceListener>() {
                        public boolean run(IBlackTortoiseServiceListener item)
                                throws RemoteException {
                            item.onReceivePacket(packet);
                            return true;
                        };
                    });
        };

        @Override
        public void onDeviceStateChanged(final DeviceState state, final DeviceEventCode code,
                final DeviceInfo deviceInfo) {
            AidlUtil.callMethods(mServiceListeners,
                    new CallFunction<IBlackTortoiseServiceListener>() {
                        public boolean run(IBlackTortoiseServiceListener item)
                                throws RemoteException {

                            item.onDeviceStateChanged(state, code, getCurrentDeviceInfo());
                            return true;
                        };
                    });
            handleConnectedNotification(state == DeviceState.CONNECTED, getCurrentDeviceInfo());
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
    public boolean onUnbind(Intent intent) {
        if (mDeviceAdapter == null) {
            stopSelf();
        }
        return false;
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
        disconnect();
        unregisterReceiver(mUsbReceiver);
    }

    private void connect(DeviceInfo deviceInfo) {
        disconnect();

        if (deviceInfo.getDeviceType() == DeviceType.TCP) {
            mDeviceAdapter = new RemoteDeviceAdapter(mDeviceAdapterListener, true,
                    deviceInfo.getTcpHostName(), deviceInfo.getTcpPort());
            try {
                mDeviceAdapter.startAdapter();
            } catch (InterruptedException e) {
                // Impossible
                throw new RuntimeException(e);
            }
        } else if (deviceInfo.getDeviceType() == DeviceType.USB) {
            UsbManager usbManager = (UsbManager)getSystemService(USB_SERVICE);
            Map<String, UsbDevice> devices = usbManager.getDeviceList();
            UsbDevice usbDevice = devices.get(deviceInfo.getUsbDeviceKey());
            if (usbManager.hasPermission(usbDevice)) {
                // If service already has permission, it start thread.
                mDeviceAdapter = new LocalDeviceAdapter(mDeviceAdapterListener, true, usbManager,
                        usbDevice);
                try {
                    mDeviceAdapter.startAdapter();
                } catch (InterruptedException e) {
                    // Impossible
                    throw new RuntimeException(e);
                }
            } else {
                // Request
                Intent intent = new Intent(ACTION_USB_PERMISSION);
                intent.putExtra(EXTRA_USB_DEVICE_KEY, deviceInfo.getUsbDeviceKey());
                PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
                usbManager.requestPermission(usbDevice, pIntent);
            }
        } else {
            mDeviceAdapter = new DummyDeviceAdapter(mDeviceAdapterListener, sHandler);
            try {
                mDeviceAdapter.startAdapter();
            } catch (InterruptedException e) {
                // Impossible
                throw new RuntimeException(e);
            }
        }
    }

    private void disconnect() {
        if (mDeviceAdapter != null) {
            try {
                mDeviceAdapter.stopAdapter();
                mDeviceAdapter = null;
            } catch (InterruptedException e) {
                // Impossible
                throw new RuntimeException(e);
            }
        }
    }

    private void handleConnectedNotification(boolean connected, DeviceInfo deviceInfo) {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (connected) {
            Intent intent = new Intent(this, SelectDeviceActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentIntent(pIntent);
            builder.setContentTitle(getText(R.string.notification_title_connected));
            builder.setContentText(deviceInfo.getLabel());
            builder.setSmallIcon(R.drawable.ic_launcher);
            @SuppressWarnings("deprecation")
            Notification nortification = builder.getNotification();
            manager.notify(NOTIFICATION_CONNECTED_ID, nortification);
        } else {
            manager.cancel(NOTIFICATION_CONNECTED_ID);
        }
    }

    private DeviceInfo getCurrentDeviceInfo() {
        return pickDeviceInfo(mDeviceAdapter);
    }

    private static DeviceInfo pickDeviceInfo(IDeviceAdapter adapter) {
        if (adapter != null) {
            return adapter.getDeviceInfo();
        } else {
            return null;
        }
    }
}
