
package net.blacktortoise.android.seed;

import net.blacktortoise.android.camera.ICameraManager;
import net.blacktortoise.androidlib.IDeviceAdapter;
import net.blacktortoise.androidlib.IDeviceAdapterListener;
import android.content.Context;

public interface IDeviceAdapterSeed {
    public IDeviceAdapter createDeviceAdapter(Context context, IDeviceAdapterListener listener);

    public ICameraManager createCameraManager();
}
