
package net.blacktortoise.android.fragment;

import net.blacktortoise.android.BlackTortoiseService;
import net.blacktortoise.android.R;
import net.blacktortoise.android.SelectDeviceActivity;
import net.blacktortoise.androidlib.IBlackTortoiseService;
import net.blacktortoise.androidlib.data.DeviceInfo;
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

public class ConnectFragment extends BaseFragment implements OnClickListener {

    private IBlackTortoiseService mService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mService = IBlackTortoiseService.Stub.asInterface(binder);
            final DeviceInfo currentDeviceKey;
            try {
                currentDeviceKey = mService.getCurrentDeviceInfo();
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

        // Binds event listener
        view.findViewById(R.id.startButton).setOnClickListener(this);
        view.findViewById(R.id.goToSelectDeviceButton).setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = new Intent(getContext(), BlackTortoiseService.class);
        getContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unbindService(mServiceConnection);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.startButton) {
            connectToService();
        } else if (v.getId() == R.id.goToSelectDeviceButton) {
            Intent intent = new Intent(getContext(), SelectDeviceActivity.class);
            startActivity(intent);
        }
    }

    private void connectToService() {
        MainMenuFragment nextFragment = new MainMenuFragment();
        replacePrimaryFragment(nextFragment, false);
    }
}
