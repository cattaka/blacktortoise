
package net.cattaka.android.ultimatetank.fragment;

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
public class BaseFragment extends Fragment {
    public interface IBaseFragmentAdapter {
        public Object getSystemService(String name);

        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter);

        public void unregisterReceiver(BroadcastReceiver receiver);
    }

    public IBaseFragmentAdapter getBaseFragmentAdapter() {
        return (IBaseFragmentAdapter)getActivity();
    }

    public Context getContext() {
        return getActivity();
    }

    /** Do only delegation */
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return getActivity().registerReceiver(receiver, filter);
    }

    /** Do only delegation */
    public void unregisterReceiver(BroadcastReceiver receiver) {
        getActivity().unregisterReceiver(receiver);
    }
}
