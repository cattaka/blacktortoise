
package net.blacktortoise.android.seed;

import net.blacktortoise.android.camera.DeviceCameraManager;
import net.blacktortoise.android.camera.ICameraManager;
import net.blacktortoise.androidlib.IDeviceAdapter;
import net.blacktortoise.androidlib.IDeviceAdapterListener;
import net.blacktortoise.androidlib.adapter.BtServiceAdapter;
import android.content.Context;

public class BtServiceDeviceAdapterSeed implements IDeviceAdapterSeed {

    public BtServiceDeviceAdapterSeed() {
        super();
    }

    @Override
    public IDeviceAdapter createDeviceAdapter(Context context, IDeviceAdapterListener listener) {
        return new BtServiceAdapter(listener, context);
    }

    @Override
    public ICameraManager createCameraManager() {
        return new DeviceCameraManager();
    }
}
