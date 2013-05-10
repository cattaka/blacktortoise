
package net.cattaka.android.ultimatetank;

import java.util.ArrayList;
import java.util.List;

import net.cattaka.android.ultimatetank.camera.ICameraManager;
import net.cattaka.android.ultimatetank.fragment.BaseFragment.IBaseFragmentAdapter;
import net.cattaka.android.ultimatetank.fragment.ConnectFragment;
import net.cattaka.android.ultimatetank.usb.ICommandAdapter;
import net.cattaka.android.ultimatetank.usb.IMySocketPrepareTask;
import net.cattaka.android.ultimatetank.usb.MyConnectionThread;
import net.cattaka.android.ultimatetank.usb.data.MyPacket;
import net.cattaka.android.ultimatetank.usb.data.MyPacketFactory;
import net.cattaka.libgeppa.data.ConnectionCode;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.thread.IConnectionThreadListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.WindowManager;

public class MainActivity extends Activity implements IBaseFragmentAdapter {
    private MainActivity me = this;

    private ICommandAdapter mCommandAdapter;

    private IMySocketPrepareTask mCurrentPrepareTask;

    private List<IConnectionThreadListener<MyPacket>> mConnectionThreadListeners;

    private IConnectionThreadListener<MyPacket> mConnectionThreadListener = new IConnectionThreadListener<MyPacket>() {
        private ProgressDialog nowConnectiondDialog;

        @Override
        public void onReceive(MyPacket packet) {
            { // Notifies event to children
                for (IConnectionThreadListener<MyPacket> listener : mConnectionThreadListeners) {
                    listener.onReceive(packet);
                }
            }
        }

        @Override
        public void onConnectionStateChanged(ConnectionState state, ConnectionCode code) {
            // if (state == ConnectionState.CLOSED) {
            // FragmentManager fm = getFragmentManager();
            // for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            // fm.popBackStack();
            // }
            // ConnectFragment nextFragment = new ConnectFragment();
            // replacePrimaryFragment(nextFragment, false);
            // }
            if (state == ConnectionState.CONNECTING) {
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
                for (IConnectionThreadListener<MyPacket> listener : mConnectionThreadListeners) {
                    listener.onConnectionStateChanged(state, code);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        mConnectionThreadListeners = new ArrayList<IConnectionThreadListener<MyPacket>>();

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
            if (mCurrentPrepareTask != null) {
                startConnectionThread(mCurrentPrepareTask);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopConnectionThread();
    }

    public void startConnectionThread(IMySocketPrepareTask prepareTask) {
        mCurrentPrepareTask = prepareTask;

        stopConnectionThread();

        mCurrentPrepareTask.setup(this);

        mCommandAdapter = new MyConnectionThread(mCurrentPrepareTask, new MyPacketFactory(),
                mConnectionThreadListener);

        try {
            mCommandAdapter.startThread();
        } catch (InterruptedException e) {
            // Impossible
            throw new RuntimeException(e);
        }
    }

    public void startConnectionThreadDirect(ICommandAdapter adapter) {
        mCurrentPrepareTask = null;

        stopConnectionThread();

        mCommandAdapter = adapter;

        try {
            mCommandAdapter.startThread();
        } catch (InterruptedException e) {
            // Impossible
            throw new RuntimeException(e);
        }
    }

    public void stopConnectionThread() {
        if (mCommandAdapter != null) {
            try {
                mCommandAdapter.stopThread();
            } catch (InterruptedException e) {
                // Impossible
                throw new RuntimeException(e);
            }
            mCommandAdapter = null;
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
        if (mCurrentPrepareTask != null) {
            return mCurrentPrepareTask.createCameraManager();
        } else {
            return null;
        }
    }

    @Override
    public ICommandAdapter getCommandAdapter() {
        return mCommandAdapter;
    }

    @Override
    public boolean registerConnectionThreadListener(IConnectionThreadListener<MyPacket> listener) {
        if (!mConnectionThreadListeners.contains(listener)) {
            mConnectionThreadListeners.add(listener);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unregisterConnectionThreadListener(IConnectionThreadListener<MyPacket> listener) {
        return mConnectionThreadListeners.remove(listener);
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
