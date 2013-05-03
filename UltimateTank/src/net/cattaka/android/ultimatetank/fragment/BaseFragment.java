
package net.cattaka.android.ultimatetank.fragment;

import net.cattaka.android.ultimatetank.usb.ICommandAdapter;
import net.cattaka.android.ultimatetank.usb.data.MyPacket;
import net.cattaka.libgeppa.data.ConnectionCode;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.thread.IConnectionThreadListener;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;

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

        public void startConnectionThread(UsbDevice usbDevice);

        public void replacePrimaryFragment(Fragment fragment, boolean withBackStack);

        public ICommandAdapter getCommandAdapter();
    }

    public IBaseFragmentAdapter getBaseFragmentAdapter() {
        return (IBaseFragmentAdapter)getActivity();
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
    public void connectUsbDevice(UsbDevice usbDevice) {
        getBaseFragmentAdapter().startConnectionThread(usbDevice);
    }

    /** Do only delegation */
    public void replacePrimaryFragment(Fragment fragment, boolean withBackStack) {
        getBaseFragmentAdapter().replacePrimaryFragment(fragment, withBackStack);
    }

    public ICommandAdapter getCommandAdapter() {
        return getBaseFragmentAdapter().getCommandAdapter();
    }
}
