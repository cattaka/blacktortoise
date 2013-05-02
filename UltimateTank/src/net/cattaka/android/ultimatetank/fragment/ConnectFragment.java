
package net.cattaka.android.ultimatetank.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.cattaka.android.ultimatetank.R;
import net.cattaka.android.ultimatetank.usb.UsbClass;
import net.cattaka.libgeppa.data.ConnectionCode;
import net.cattaka.libgeppa.data.ConnectionState;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ConnectFragment extends BaseFragment implements OnItemClickListener {
    private ListView mUsbDeviceList;

    protected static final String ACTION_USB_PERMISSION = "net.cattaka.android.ultimatetank.fragment.action_permission";

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

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                refleshUsbDeviceList();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                refleshUsbDeviceList();
            } else if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                refleshUsbDeviceList();
                String itemKey = intent.getStringExtra(EXTRA_USB_DEVICE_KEY);
                if (itemKey != null) {
                    onSelectItem(itemKey);
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, null);
        mUsbDeviceList = (ListView)view.findViewById(R.id.usbDeviceList);

        // Binds event listener
        mUsbDeviceList.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refleshUsbDeviceList();

        { // Registers receiver for USB attach
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(ACTION_USB_PERMISSION);
            registerReceiver(mUsbReceiver, filter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        { // Unregisters receiver for USB attach
            unregisterReceiver(mUsbReceiver);
        }
    }

    private void refleshUsbDeviceList() {
        UsbManager usbman = (UsbManager)getBaseFragmentAdapter().getSystemService(
                Context.USB_SERVICE);

        List<ListItem> items = new ArrayList<ConnectFragment.ListItem>();
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

        ArrayAdapter<ListItem> adapter = new ArrayAdapter<ConnectFragment.ListItem>(getContext(),
                android.R.layout.simple_list_item_1, items);
        mUsbDeviceList.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        if (parent.getId() == R.id.usbDeviceList) {
            ListItem item = (ListItem)mUsbDeviceList.getItemAtPosition(position);
            onSelectItem(item.key);
        }
    }

    private void onSelectItem(String itemKey) {
        UsbManager usbman = (UsbManager)getBaseFragmentAdapter().getSystemService(
                Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceMap = usbman.getDeviceList();
        UsbDevice usbDevice = deviceMap.get(itemKey);
        if (usbDevice == null) {
            // Unexpected behavior
            refleshUsbDeviceList();
        } else {
            if (usbman.hasPermission(usbDevice)) {
                // OK
                getBaseFragmentAdapter().startConnectionThread(usbDevice);
            } else {
                // requests permission to use device
                Intent intent = new Intent(ACTION_USB_PERMISSION);
                intent.putExtra(EXTRA_USB_DEVICE_KEY, itemKey);
                PendingIntent pIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
                usbman.requestPermission(usbDevice, pIntent);
            }
        }
    }

    @Override
    public void onConnectionStateChanged(ConnectionState state, ConnectionCode code) {
        super.onConnectionStateChanged(state, code);
        if (state == ConnectionState.CONNECTED) {
            MainMenuFragment nextFragment = new MainMenuFragment();
            replacePrimaryFragment(nextFragment, false);
        }
    }
}
