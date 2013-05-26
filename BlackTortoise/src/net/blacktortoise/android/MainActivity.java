
package net.blacktortoise.android;

import java.util.ArrayList;
import java.util.List;

import net.blacktortoise.android.camera.ICameraManager;
import net.blacktortoise.android.fragment.BaseFragment.IBaseFragmentAdapter;
import net.blacktortoise.android.fragment.ConnectFragment;
import net.blacktortoise.android.seed.IDeviceAdapterSeed;
import net.blacktortoise.androidlib.IDeviceAdapter;
import net.blacktortoise.androidlib.IDeviceAdapterListener;
import net.blacktortoise.androidlib.IDeviceCommandAdapter;
import net.blacktortoise.androidlib.data.DeviceEventCode;
import net.blacktortoise.androidlib.data.DeviceState;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.WindowManager;

public class MainActivity extends Activity implements IBaseFragmentAdapter {
    private MainActivity me = this;

    private IDeviceAdapter mDeviceAdapter;

    private IDeviceAdapterSeed mDeviceAdapterSeed;

    private List<IDeviceAdapterListener> mDeviceAdapterListeners;

    private IDeviceAdapterListener mConnectionThreadListener = new IDeviceAdapterListener() {
        private ProgressDialog nowConnectiondDialog;

        @Override
        public void onReceiveEcho(byte[] data) {
            // Notifies event to children
            for (IDeviceAdapterListener listener : mDeviceAdapterListeners) {
                listener.onReceiveEcho(data);
            }
        }

        @Override
        public void onReceiveCameraImage(int cameraIdx, Bitmap bitmap) {
            // Notifies event to children
            for (IDeviceAdapterListener listener : mDeviceAdapterListeners) {
                listener.onReceiveCameraImage(cameraIdx, bitmap);
            }
        }

        @Override
        public void onDeviceStateChanged(DeviceState state, DeviceEventCode code) {
            // if (state == ConnectionState.CLOSED) {
            // FragmentManager fm = getFragmentManager();
            // for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            // fm.popBackStack();
            // }
            // ConnectFragment nextFragment = new ConnectFragment();
            // replacePrimaryFragment(nextFragment, false);
            // }
            if (state == DeviceState.CONNECTING) {
                if (nowConnectiondDialog == null) {
                    nowConnectiondDialog = new ProgressDialog(me);
                    nowConnectiondDialog.setMessage(me.getText(R.string.msg_now_connecting));
                    nowConnectiondDialog
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    stopConnectionThread();
                                }
                            });
                    nowConnectiondDialog.show();
                }
            } else // if (state == ConnectionState.CONNECTED || state ==
                   // ConnectionState.CLOSED)
            {
                if (nowConnectiondDialog != null) {
                    nowConnectiondDialog.dismiss();
                    nowConnectiondDialog = null;
                }
            }
            { // Notifies event to children
                for (IDeviceAdapterListener listener : mDeviceAdapterListeners) {
                    listener.onDeviceStateChanged(state, code);
                }
            }
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
        { // If last connected device exists, start conectionThread again.
            if (mDeviceAdapterSeed != null) {
                startDeviceAdapter(mDeviceAdapterSeed);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopConnectionThread();
    }

    public void startDeviceAdapter(IDeviceAdapterSeed deviceAdapterSeed) {
        mDeviceAdapterSeed = deviceAdapterSeed;

        stopConnectionThread();

        mDeviceAdapter = mDeviceAdapterSeed.createDeviceAdapter(this, mConnectionThreadListener);

        try {
            mDeviceAdapter.startAdapter();
        } catch (InterruptedException e) {
            // Impossible
            throw new RuntimeException(e);
        }
    }

    public void stopConnectionThread() {
        if (mDeviceAdapter != null) {
            try {
                mDeviceAdapter.stopAdapter();
            } catch (InterruptedException e) {
                // Impossible
                throw new RuntimeException(e);
            }
            mDeviceAdapter = null;
        }
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
    public ICameraManager createCameraManager() {
        if (mDeviceAdapterSeed != null && mDeviceAdapter != null) {
            return mDeviceAdapterSeed.createCameraManager();
        } else {
            return null;
        }
    }

    @Override
    public IDeviceCommandAdapter getCommandAdapter() {
        return mDeviceAdapter;
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

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0 && mDeviceAdapter != null) {
            stopConnectionThread();
            mDeviceAdapterSeed = null;
            ConnectFragment next = new ConnectFragment();
            replacePrimaryFragment(next, false);
            return;
        } else {
            super.onBackPressed();
        }
    }
}
