
package net.cattaka.android.ultimatetank.common.adapter;

import net.cattaka.android.ultimatetank.common.IDeviceAdapterListener;
import net.cattaka.android.ultimatetank.net.RemoteSocketPrepareTask;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;

public class RemoteDeviceAdapter extends BtConnectionAdapter {
    private String mHostname;

    private int mPort;

    public RemoteDeviceAdapter(IDeviceAdapterListener listener, boolean useMainLooperForListener,
            String hostname, int port) {
        super(listener, useMainLooperForListener);
        if (hostname == null) {
            throw new NullPointerException();
        }
        mHostname = hostname;
        mPort = port;
    }

    @Override
    protected IRawSocketPrepareTask createRawSocketPrepareTask() {
        return new RemoteSocketPrepareTask(mHostname, mPort);
    }
}
