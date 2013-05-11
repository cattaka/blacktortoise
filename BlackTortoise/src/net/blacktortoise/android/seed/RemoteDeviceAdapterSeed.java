
package net.blacktortoise.android.seed;

import net.blacktortoise.android.camera.ICameraManager;
import net.blacktortoise.android.camera.RemoteCameraManager;
import net.blacktortoise.android.common.IDeviceAdapter;
import net.blacktortoise.android.common.IDeviceAdapterListener;
import net.blacktortoise.android.common.adapter.RemoteDeviceAdapter;
import android.content.Context;

public class RemoteDeviceAdapterSeed implements IDeviceAdapterSeed {
    private String mHostname;

    private int mPort;

    public RemoteDeviceAdapterSeed(String hostname, int port) {
        super();
        mHostname = hostname;
        mPort = port;
    }

    @Override
    public IDeviceAdapter createDeviceAdapter(Context context, IDeviceAdapterListener listener) {
        return new RemoteDeviceAdapter(listener, true, mHostname, mPort);
    }

    @Override
    public ICameraManager createCameraManager() {
        return new RemoteCameraManager();
    }
}
