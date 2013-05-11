
package net.cattaka.android.ultimatetank.seed;

import net.cattaka.android.ultimatetank.camera.DeviceCameraManager;
import net.cattaka.android.ultimatetank.camera.ICameraManager;
import net.cattaka.android.ultimatetank.common.IDeviceAdapter;
import net.cattaka.android.ultimatetank.common.IDeviceAdapterListener;
import net.cattaka.android.ultimatetank.common.adapter.DummyDeviceAdapter;
import android.content.Context;
import android.os.Handler;

public class DummyDeviceAdapterSeed implements IDeviceAdapterSeed {
    private Handler mHandler;

    public DummyDeviceAdapterSeed(Handler handler) {
        super();
        mHandler = handler;
    }

    @Override
    public IDeviceAdapter createDeviceAdapter(Context context, IDeviceAdapterListener listener) {
        return new DummyDeviceAdapter(listener, mHandler);
    }

    @Override
    public ICameraManager createCameraManager() {
        return new DeviceCameraManager();
    }
}
