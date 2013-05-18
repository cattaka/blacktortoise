
package net.blacktortoise.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.blacktortoise.android.common.data.BtPacket;
import net.blacktortoise.android.common.data.DeviceEventCode;
import net.blacktortoise.android.common.data.DeviceState;
import net.blacktortoise.android.db.BtDbHelper;
import net.blacktortoise.android.dialog.EditAddrresDialog;
import net.blacktortoise.android.usb.UsbClass;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ConnectLocalDeviceActivity extends Activity implements OnClickListener,
        OnItemClickListener {
    protected static final String EXTRA_USB_DEVICE_KEY = "usbDevicekey";

    private static class ListItem {
        String label;

        String key;

        public ListItem(String label, String key) {
            super();
            this.label = label;
            this.key = key;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                refleshUsbDeviceList();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                refleshUsbDeviceList();
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IBlackTortoiseService.Stub.asInterface(service);
            if (mServiceListenerSeq < 0) {
                try {
                    mServiceListenerSeq = mService.registerServiceListener(mServiceListener);
                    String deviceKey = mService.getCurrentDeviceKey();
                    updateSelectedUsbDevice(deviceKey);
                } catch (RemoteException e) {
                    // Nothing to do
                    throw new RuntimeException(e);
                }
            }
        }
    };

    private ConnectLocalDeviceActivity me = this;

    private int mServiceListenerSeq = -1;

    private IBlackTortoiseServiceListener mServiceListener = new IBlackTortoiseServiceListener.Stub() {
        private ProgressDialog mProgressDialog;

        @Override
        public void onReceive(BtPacket packet) throws RemoteException {
            // ignore
        }

        public void onDeviceStateChanged(final DeviceState state, final DeviceEventCode code,
                final String deviceKey) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state == DeviceState.CONNECTING) {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        mProgressDialog = new ProgressDialog(me);
                        mProgressDialog.setMessage(getString(R.string.msg_now_connecting));
                        mProgressDialog.show();
                    } else {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                    }
                    updateSelectedUsbDevice(deviceKey);
                }
            });
        }
    };

    private IBlackTortoiseService mService;

    private BtDbHelper mDbHelper;

    private EditAddrresDialog mEditAddrresDialog;

    private ListView mUsbDeviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_local_device);

        // Pickup view
        mUsbDeviceList = (ListView)findViewById(R.id.usbDeviceList);

        // Binds event listener
        mUsbDeviceList.setOnItemClickListener(this);

        mUsbDeviceList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent service = new Intent(this, BlackTortoiseService.class);
        startService(service);
        bindService(service, mServiceConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceListenerSeq >= 0) {
            try {
                mService.unregisterServiceListener(mServiceListenerSeq);
                mServiceListenerSeq = -1;
            } catch (RemoteException e) {
                // Nothing to do
                throw new RuntimeException(e);
            }
        }
        unbindService(mServiceConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDbHelper = new BtDbHelper(this);

        refleshUsbDeviceList();

        { // Registers receiver for USB attach
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            registerReceiver(mUsbReceiver, filter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        { // Unregisters receiver for USB attach
            unregisterReceiver(mUsbReceiver);
        }
        mDbHelper.close();
        mDbHelper = null;
    }

    private void refleshUsbDeviceList() {
        UsbManager usbman = (UsbManager)getSystemService(Context.USB_SERVICE);

        List<ListItem> items = new ArrayList<ConnectLocalDeviceActivity.ListItem>();
        { // Add special item
            items.add(new ListItem(getString(R.string.item_disconnect), null));
        }
        { // Add an item of dummy
            items.add(new ListItem(getString(R.string.item_dummy), Constants.DUMMY_DEVICE_ID));
        }
        { // Creates list items
            HashMap<String, UsbDevice> deviceMap = usbman.getDeviceList();
            for (Entry<String, UsbDevice> entry : deviceMap.entrySet()) {
                UsbDevice d = entry.getValue();
                String name = UsbClass.parce(d.getDeviceClass()).name();
                String label = String.format("%s(%04X:%04X)", name, d.getVendorId(),
                        d.getProductId());
                items.add(new ListItem(label, entry.getKey()));
            }
        }

        ArrayAdapter<ListItem> adapter = new ArrayAdapter<ConnectLocalDeviceActivity.ListItem>(
                this, android.R.layout.simple_list_item_single_choice, items);
        mUsbDeviceList.setAdapter(adapter);
    }

    private void updateSelectedUsbDevice(String deviceId) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<ListItem> adapter = (ArrayAdapter<ConnectLocalDeviceActivity.ListItem>)mUsbDeviceList
                .getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            ListItem item = adapter.getItem(i);
            mUsbDeviceList.setItemChecked(i, equalsStr(item.key, deviceId));
        }
    }

    private boolean equalsStr(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        } else {
            return s1.equals(s2);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addSocketAddressButton) {
            mEditAddrresDialog.show(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.usbDeviceList) {
            ListItem item = (ListItem)mUsbDeviceList.getItemAtPosition(position);
            onSelectUsbItem(item.key);
        }
    }

    private void onSelectUsbItem(String itemKey) {
        try {
            if (itemKey != null) {
                mService.connect(itemKey);
            } else {
                mService.disconnect();
            }
        } catch (RemoteException e) {
            // Impossible
            throw new RuntimeException(e);
        }
    }
}
