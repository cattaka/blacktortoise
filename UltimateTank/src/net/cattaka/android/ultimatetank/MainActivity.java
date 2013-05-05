
package net.cattaka.android.ultimatetank;

import net.cattaka.android.ultimatetank.fragment.BaseFragment;
import net.cattaka.android.ultimatetank.fragment.BaseFragment.IBaseFragmentAdapter;
import net.cattaka.android.ultimatetank.fragment.ConnectFragment;
import net.cattaka.android.ultimatetank.usb.ICommandAdapter;
import net.cattaka.android.ultimatetank.usb.MyConnectionThread;
import net.cattaka.android.ultimatetank.usb.data.MyPacket;
import net.cattaka.android.ultimatetank.usb.data.MyPacketFactory;
import net.cattaka.libgeppa.data.ConnectionCode;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;
import net.cattaka.libgeppa.thread.IConnectionThreadListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity implements IBaseFragmentAdapter {
    private MainActivity me = this;

    private MyConnectionThread mConnectionThread;

    private IRawSocketPrepareTask mCurrentPrepareTask;

    private IConnectionThreadListener<MyPacket> mConnectionThreadListener = new IConnectionThreadListener<MyPacket>() {
        private ProgressDialog nowConnectiondDialog;

        @Override
        public void onReceive(MyPacket packet) {
            { // Notifies event to child fragment
                Fragment fragment = getFragmentManager().findFragmentById(R.id.primaryFragment);
                if (fragment instanceof BaseFragment) {
                    ((BaseFragment)fragment).onReceive(packet);
                } else {
                    // impossible
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
            { // Notifies event to child fragment
                Fragment fragment = getFragmentManager().findFragmentById(R.id.primaryFragment);
                if (fragment instanceof BaseFragment) {
                    ((BaseFragment)fragment).onConnectionStateChanged(state, code);
                } else {
                    // impossible
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    public void startConnectionThread(IRawSocketPrepareTask prepareTask) {
        mCurrentPrepareTask = prepareTask;

        stopConnectionThread();

        mCurrentPrepareTask.setup(this);

        mConnectionThread = new MyConnectionThread(mCurrentPrepareTask, new MyPacketFactory(),
                mConnectionThreadListener);

        try {
            mConnectionThread.startThread();
        } catch (InterruptedException e) {
            // Impossible
            throw new RuntimeException(e);
        }
    }

    public void stopConnectionThread() {
        if (mConnectionThread != null) {
            try {
                mConnectionThread.stopThread();
            } catch (InterruptedException e) {
                // Impossible
                throw new RuntimeException(e);
            }
            mConnectionThread = null;
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
    public ICommandAdapter getCommandAdapter() {
        return mConnectionThread;
    }
}
