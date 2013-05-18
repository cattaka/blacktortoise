
package net.blacktortoise.android.seed;

import net.blacktortoise.android.camera.DeviceCameraManager;
import net.blacktortoise.android.camera.ICameraManager;
import net.blacktortoise.android.common.IDeviceAdapter;
import net.blacktortoise.android.common.IDeviceAdapterListener;
import net.blacktortoise.android.common.adapter.BtServiceAdapter;
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
