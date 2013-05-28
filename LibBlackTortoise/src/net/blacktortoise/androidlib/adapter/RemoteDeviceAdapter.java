
package net.blacktortoise.androidlib.adapter;

import net.blacktortoise.androidlib.IDeviceAdapterListener;
import net.blacktortoise.androidlib.data.DeviceInfo;
import net.blacktortoise.androidlib.net.RemoteSocketPrepareTask;
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

    @Override
    public DeviceInfo getDeviceInfo() {
        return DeviceInfo.createTcp(mHostname, mPort, true);
    }

    @Override
    public boolean isCameraSupported() {
        return true;
    }
}
