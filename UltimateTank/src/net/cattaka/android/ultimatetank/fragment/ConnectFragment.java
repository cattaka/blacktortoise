
package net.cattaka.android.ultimatetank.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.cattaka.android.ultimatetank.R;
import net.cattaka.android.ultimatetank.ServiceCommandAdapter;
import net.cattaka.android.ultimatetank.db.UltimateTankDbHelper;
import net.cattaka.android.ultimatetank.dialog.EditAddrresDialog;
import net.cattaka.android.ultimatetank.dialog.EditAddrresDialog.IEditAddrresDialogListener;
import net.cattaka.android.ultimatetank.entity.MySocketAddress;
import net.cattaka.android.ultimatetank.net.RemoteSocketPrepareTask;
import net.cattaka.android.ultimatetank.usb.DummySocketPrepareTask;
import net.cattaka.android.ultimatetank.usb.UsbClass;
import net.cattaka.libgeppa.data.ConnectionCode;
import net.cattaka.libgeppa.data.ConnectionState;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ConnectFragment extends BaseFragment implements OnClickListener, OnItemClickListener,
        OnItemLongClickListener, IEditAddrresDialogListener {
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
            }
        }
    };

    private UltimateTankDbHelper mDbHelper;

    private EditAddrresDialog mEditAddrresDialog;

    private ListView mUsbDeviceList;

    private ListView mSocketAddressList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, null);
        mUsbDeviceList = (ListView)view.findViewById(R.id.usbDeviceList);
        mSocketAddressList = (ListView)view.findViewById(R.id.socketAddressList);

        // Binds event listener
        view.findViewById(R.id.useDummyButton).setOnClickListener(this);
        view.findViewById(R.id.addSocketAddressButton).setOnClickListener(this);
        mUsbDeviceList.setOnItemClickListener(this);
        mSocketAddressList.setOnItemClickListener(this);
        mSocketAddressList.setOnItemLongClickListener(this);

        mEditAddrresDialog = EditAddrresDialog.createEditAddrresDialog(getContext(), this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mDbHelper = new UltimateTankDbHelper(getContext());

        refleshUsbDeviceList();
        refleshSocketAddressList();

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

    private void refleshSocketAddressList() {
        List<MySocketAddress> items = mDbHelper.findMySocketAddresses();
        ArrayAdapter<MySocketAddress> adapter = new ArrayAdapter<MySocketAddress>(getContext(),
                android.R.layout.simple_list_item_1, items);
        mSocketAddressList.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addSocketAddressButton) {
            mEditAddrresDialog.show(null);
        } else if (v.getId() == R.id.useDummyButton) {
            DummySocketPrepareTask prepareTask = new DummySocketPrepareTask();
            getBaseFragmentAdapter().startConnectionThread(prepareTask);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        if (parent.getId() == R.id.usbDeviceList) {
            ListItem item = (ListItem)mUsbDeviceList.getItemAtPosition(position);
            onSelectUsbItem(item.key);
        } else if (parent.getId() == R.id.socketAddressList) {
            MySocketAddress item = (MySocketAddress)parent.getItemAtPosition(position);
            onSelectSocketAddress(item);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.socketAddressList) {
            MySocketAddress item = (MySocketAddress)parent.getItemAtPosition(position);
            mEditAddrresDialog.show(item);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see EditAddrresDialog.IEditAddrresDialogListener
     */
    @Override
    public void onEditAddrresDialogFinished(MySocketAddress result) {
        mDbHelper.registerMySocketAddress(result);
        refleshSocketAddressList();
    };

    /**
     * @see EditAddrresDialog.IEditAddrresDialogListener
     */
    @Override
    public void onEditAddrresDialogCanceled() {
        // none
    }

    @Override
    public void onEditAddrresDialogDelete(Long id) {
        if (id != null) {
            mDbHelper.deleteMySocketAddress(id);
            refleshSocketAddressList();
        }
    }

    private void onSelectUsbItem(String itemKey) {
        UsbManager usbman = (UsbManager)getBaseFragmentAdapter().getSystemService(
                Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceMap = usbman.getDeviceList();
        UsbDevice usbDevice = deviceMap.get(itemKey);
        if (usbDevice == null) {
            // Unexpected behavior
            refleshUsbDeviceList();
        } else {
            getBaseFragmentAdapter().startConnectionThreadDirect(
                    new ServiceCommandAdapter(getContext(), itemKey));
        }
    }

    private void onSelectSocketAddress(MySocketAddress item) {
        RemoteSocketPrepareTask prepareTask = new RemoteSocketPrepareTask(item.getHostName(),
                item.getPort());
        getBaseFragmentAdapter().startConnectionThread(prepareTask);
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
