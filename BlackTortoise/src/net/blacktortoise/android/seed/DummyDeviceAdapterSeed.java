
package net.blacktortoise.android.seed;

import net.blacktortoise.android.camera.DeviceCameraManager;
import net.blacktortoise.android.camera.ICameraManager;
import net.blacktortoise.androidlib.IDeviceAdapter;
import net.blacktortoise.androidlib.IDeviceAdapterListener;
import net.blacktortoise.androidlib.adapter.DummyDeviceAdapter;
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
