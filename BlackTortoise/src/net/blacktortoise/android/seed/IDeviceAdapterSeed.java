
package net.blacktortoise.android.seed;

import net.blacktortoise.android.camera.ICameraManager;
import net.blacktortoise.android.common.IDeviceAdapter;
import net.blacktortoise.android.common.IDeviceAdapterListener;
import android.content.Context;

public interface IDeviceAdapterSeed {
    public IDeviceAdapter createDeviceAdapter(Context context, IDeviceAdapterListener listener);

    public ICameraManager createCameraManager();
}
