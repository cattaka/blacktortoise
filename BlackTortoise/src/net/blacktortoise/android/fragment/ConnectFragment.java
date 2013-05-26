
package net.blacktortoise.android.fragment;

import java.util.List;

import net.blacktortoise.android.BlackTortoiseService;
import net.blacktortoise.android.SelectDeviceActivity;
import net.blacktortoise.android.IBlackTortoiseService;
import net.blacktortoise.android.R;
import net.blacktortoise.android.common.data.DeviceEventCode;
import net.blacktortoise.android.common.data.DeviceState;
import net.blacktortoise.android.db.BtDbHelper;
import net.blacktortoise.android.dialog.EditAddrresDialog;
import net.blacktortoise.android.dialog.EditAddrresDialog.IEditAddrresDialogListener;
import net.blacktortoise.android.entity.MySocketAddress;
import net.blacktortoise.android.seed.BtServiceDeviceAdapterSeed;
import net.blacktortoise.android.seed.RemoteDeviceAdapterSeed;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
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
    private BtDbHelper mDbHelper;

    private EditAddrresDialog mEditAddrresDialog;

    private ListView mSocketAddressList;

    private IBlackTortoiseService mService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mService = IBlackTortoiseService.Stub.asInterface(binder);
            final String currentDeviceKey;
            try {
                currentDeviceKey = mService.getCurrentDeviceKey();
            } catch (RemoteException e) {
                // Nothing to do
                throw new RuntimeException(e);
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    int v = (currentDeviceKey != null) ? View.VISIBLE : View.INVISIBLE;
                    getView().findViewById(R.id.startButton).setVisibility(v);
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, null);
        mSocketAddressList = (ListView)view.findViewById(R.id.socketAddressList);

        // Binds event listener
        view.findViewById(R.id.startButton).setOnClickListener(this);
        view.findViewById(R.id.addSocketAddressButton).setOnClickListener(this);
        view.findViewById(R.id.goToSelectDeviceButton).setOnClickListener(this);
        mSocketAddressList.setOnItemClickListener(this);
        mSocketAddressList.setOnItemLongClickListener(this);

        mEditAddrresDialog = EditAddrresDialog.createEditAddrresDialog(getContext(), this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mDbHelper = new BtDbHelper(getContext());

        refleshSocketAddressList();

        Intent intent = new Intent(getContext(), BlackTortoiseService.class);
        getContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        mDbHelper.close();
        mDbHelper = null;
        getContext().unbindService(mServiceConnection);
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
        } else if (v.getId() == R.id.startButton) {
            connectToService();
        } else if (v.getId() == R.id.goToSelectDeviceButton) {
            Intent intent = new Intent(getContext(), SelectDeviceActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.socketAddressList) {
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

    private void connectToService() {
        BtServiceDeviceAdapterSeed seed = new BtServiceDeviceAdapterSeed();
        getBaseFragmentAdapter().startDeviceAdapter(seed);
    }

    private void onSelectSocketAddress(MySocketAddress item) {
        RemoteDeviceAdapterSeed seed = new RemoteDeviceAdapterSeed(item.getHostName(),
                item.getPort());
        getBaseFragmentAdapter().startDeviceAdapter(seed);
    }

    @Override
    public void onDeviceStateChanged(DeviceState state, DeviceEventCode code) {
        super.onDeviceStateChanged(state, code);
        if (state == DeviceState.CONNECTED) {
            MainMenuFragment nextFragment = new MainMenuFragment();
            replacePrimaryFragment(nextFragment, false);
        }
    }
}
