
package net.blacktortoise.android;

import java.util.ArrayList;
import java.util.List;

import net.blacktortoise.android.fragment.BaseFragment.IBaseFragmentAdapter;
import net.blacktortoise.android.fragment.ConnectFragment;
import net.blacktortoise.androidlib.BlackTortoiseFunctions;
import net.blacktortoise.androidlib.BlackTortoiseServiceWrapper;
import net.blacktortoise.androidlib.IBlackTortoiseService;
import net.blacktortoise.androidlib.IDeviceAdapterListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.view.Menu;
import android.view.WindowManager;

public class MainActivity extends Activity implements IBaseFragmentAdapter {
    private BlackTortoiseServiceWrapper mServiceWrapper;

    private List<IDeviceAdapterListener> mDeviceAdapterListeners;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceWrapper = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            IBlackTortoiseService service = IBlackTortoiseService.Stub.asInterface(binder);
            mServiceWrapper = new BlackTortoiseServiceWrapper(service);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        mDeviceAdapterListeners = new ArrayList<IDeviceAdapterListener>();

        if (getFragmentManager().findFragmentById(R.id.primaryFragment) == null) {
            ConnectFragment fragment = new ConnectFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();

            ft.add(R.id.primaryFragment, fragment);

            // トランザクションをコミットする
            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent service = BlackTortoiseFunctions.createServiceIntent();
        startService(service);
        bindService(service, mServiceConnection, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
    }

    @Override
    public void replacePrimaryFragment(Fragment fragment, boolean withBackStack) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (withBackStack) {
            ft.addToBackStack(null);
        }
        ft.replace(R.id.primaryFragment, fragment);
        ft.commit();
    }

    @Override
    public BlackTortoiseServiceWrapper getServiceWrapper() {
        return mServiceWrapper;
    }

    @Override
    public boolean registerDeviceAdapterListener(IDeviceAdapterListener listener) {
        if (!mDeviceAdapterListeners.contains(listener)) {
            mDeviceAdapterListeners.add(listener);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unregisterDeviceAdapterListener(IDeviceAdapterListener listener) {
        return mDeviceAdapterListeners.remove(listener);
    }

    @Override
    public void setKeepScreen(boolean flag) {
        if (flag) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
