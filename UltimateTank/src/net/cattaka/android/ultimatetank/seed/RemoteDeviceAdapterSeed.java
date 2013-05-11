
package net.cattaka.android.ultimatetank.seed;

import net.cattaka.android.ultimatetank.camera.ICameraManager;
import net.cattaka.android.ultimatetank.camera.RemoteCameraManager;
import net.cattaka.android.ultimatetank.common.IDeviceAdapter;
import net.cattaka.android.ultimatetank.common.IDeviceAdapterListener;
import net.cattaka.android.ultimatetank.common.adapter.RemoteDeviceAdapter;
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
