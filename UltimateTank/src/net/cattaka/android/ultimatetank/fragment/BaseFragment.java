
package net.cattaka.android.ultimatetank.fragment;

import net.cattaka.android.ultimatetank.camera.ICameraManager;
import net.cattaka.android.ultimatetank.camera.ICameraManagerAdapter;
import net.cattaka.android.ultimatetank.usb.ICommandAdapter;
import net.cattaka.android.ultimatetank.usb.IMySocketPrepareTask;
import net.cattaka.android.ultimatetank.usb.data.MyPacket;
import net.cattaka.libgeppa.data.ConnectionCode;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.thread.IConnectionThreadListener;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * This class abstract activity's functions so that avoid casting activity to
 * sub class.
 * 
 * @author cattaka
 */
public class BaseFragment extends Fragment implements IConnectionThreadListener<MyPacket> {
    public interface IBaseFragmentAdapter {
        public Object getSystemService(String name);

        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter);

        public void unregisterReceiver(BroadcastReceiver receiver);

        public void startConnectionThread(IMySocketPrepareTask prepareTask);

        public void startConnectionThreadDirect(ICommandAdapter adapter);

        public void replacePrimaryFragment(Fragment fragment, boolean withBackStack);

        public ICommandAdapter getCommandAdapter();

        public ICameraManager createCameraManager();

        public boolean registerConnectionThreadListener(IConnectionThreadListener<MyPacket> listener);

        public boolean unregisterConnectionThreadListener(
                IConnectionThreadListener<MyPacket> listener);

        public void setKeepScreen(boolean flag);
    }

    public IBaseFragmentAdapter getBaseFragmentAdapter() {
        return (IBaseFragmentAdapter)getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        getBaseFragmentAdapter().registerConnectionThreadListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getBaseFragmentAdapter().unregisterConnectionThreadListener(this);
    }

    public Context getContext() {
        return getActivity();
    }

    /** Please override if you need. */
    @Override
    public void onConnectionStateChanged(ConnectionState state, ConnectionCode code) {
        // none
    }

    /** Please override if you need. */
    @Override
    public void onReceive(MyPacket packet) {
        // none
    }

    /** Do only delegation */
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return getActivity().registerReceiver(receiver, filter);
    }

    /** Do only delegation */
    public void unregisterReceiver(BroadcastReceiver receiver) {
        getActivity().unregisterReceiver(receiver);
    }

    /** Do only delegation */
    public void connectUsbDevice(IMySocketPrepareTask prepareTask) {
        getBaseFragmentAdapter().startConnectionThread(prepareTask);
    }

    /** Do only delegation */
    public void replacePrimaryFragment(Fragment fragment, boolean withBackStack) {
        getBaseFragmentAdapter().replacePrimaryFragment(fragment, withBackStack);
    }

    public ICameraManager createCameraManager(ICameraManagerAdapter adapter) {
        ICameraManager cameraManager = getBaseFragmentAdapter().createCameraManager();
        if (cameraManager != null) {
            cameraManager.setup(adapter, getBaseFragmentAdapter());
        }
        return cameraManager;
    }

    /** Do only delegation */
    public boolean registerConnectionThreadListener(IConnectionThreadListener<MyPacket> listener) {
        return getBaseFragmentAdapter().registerConnectionThreadListener(listener);
    }

    /** Do only delegation */
    public boolean unregisterConnectionThreadListener(IConnectionThreadListener<MyPacket> listener) {
        return getBaseFragmentAdapter().unregisterConnectionThreadListener(listener);
    }

    public ICommandAdapter getCommandAdapter() {
        return getBaseFragmentAdapter().getCommandAdapter();
    }

    public void setKeepScreen(boolean flag) {
        getBaseFragmentAdapter().setKeepScreen(flag);
    }
}
