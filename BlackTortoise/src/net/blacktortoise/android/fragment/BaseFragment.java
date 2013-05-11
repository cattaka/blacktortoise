
package net.blacktortoise.android.fragment;

import net.blacktortoise.android.camera.ICameraManager;
import net.blacktortoise.android.camera.ICameraManagerAdapter;
import net.blacktortoise.android.common.IDeviceAdapterListener;
import net.blacktortoise.android.common.IDeviceCommandAdapter;
import net.blacktortoise.android.common.data.DeviceEventCode;
import net.blacktortoise.android.common.data.DeviceState;
import net.blacktortoise.android.seed.IDeviceAdapterSeed;
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
public class BaseFragment extends Fragment implements IDeviceAdapterListener {
    public interface IBaseFragmentAdapter {
        public Object getSystemService(String name);

        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter);

        public void unregisterReceiver(BroadcastReceiver receiver);

        public void startDeviceAdapter(IDeviceAdapterSeed seed);

        public void replacePrimaryFragment(Fragment fragment, boolean withBackStack);

        public IDeviceCommandAdapter getCommandAdapter();

        public ICameraManager createCameraManager();

        public boolean registerDeviceAdapterListener(IDeviceAdapterListener listener);

        public boolean unregisterDeviceAdapterListener(IDeviceAdapterListener listener);

        public void setKeepScreen(boolean flag);
    }

    public IBaseFragmentAdapter getBaseFragmentAdapter() {
        return (IBaseFragmentAdapter)getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        getBaseFragmentAdapter().registerDeviceAdapterListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getBaseFragmentAdapter().unregisterDeviceAdapterListener(this);
    }

    public Context getContext() {
        return getActivity();
    }

    /** Please override if you need. */
    @Override
    public void onDeviceStateChanged(DeviceState state, DeviceEventCode code) {
        // none
    }

    /** Please override if you need. */
    @Override
    public void onReceiveEcho(byte[] data) {
        // none
    }

    /** Please override if you need. */
    @Override
    public void onReceiveCameraImage(int cameraIdx, android.graphics.Bitmap bitmat) {
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
    public boolean registerDeviceAdapterListener(IDeviceAdapterListener listener) {
        return getBaseFragmentAdapter().registerDeviceAdapterListener(listener);
    }

    /** Do only delegation */
    public boolean unregisterDeviceAdapterListener(IDeviceAdapterListener listener) {
        return getBaseFragmentAdapter().unregisterDeviceAdapterListener(listener);
    }

    public IDeviceCommandAdapter getCommandAdapter() {
        return getBaseFragmentAdapter().getCommandAdapter();
    }

    public void setKeepScreen(boolean flag) {
        getBaseFragmentAdapter().setKeepScreen(flag);
    }
}
