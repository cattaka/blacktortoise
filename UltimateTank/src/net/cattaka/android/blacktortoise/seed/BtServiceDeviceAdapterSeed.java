
package net.cattaka.android.blacktortoise.seed;

import net.cattaka.android.blacktortoise.camera.DeviceCameraManager;
import net.cattaka.android.blacktortoise.camera.ICameraManager;
import net.cattaka.android.blacktortoise.common.IDeviceAdapter;
import net.cattaka.android.blacktortoise.common.IDeviceAdapterListener;
import net.cattaka.android.blacktortoise.common.adapter.BtServiceAdapter;
import android.content.Context;

public class BtServiceDeviceAdapterSeed implements IDeviceAdapterSeed {
    private String mDeviceId;

    public BtServiceDeviceAdapterSeed(String deviceId) {
        super();
        mDeviceId = deviceId;
    }

    @Override
    public IDeviceAdapter createDeviceAdapter(Context context, IDeviceAdapterListener listener) {
        return new BtServiceAdapter(listener, context, mDeviceId);
    }

    @Override
    public ICameraManager createCameraManager() {
        return new DeviceCameraManager();
    }
}
