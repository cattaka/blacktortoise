
package net.cattaka.android.blacktortoise.seed;

import net.cattaka.android.blacktortoise.camera.ICameraManager;
import net.cattaka.android.blacktortoise.common.IDeviceAdapter;
import net.cattaka.android.blacktortoise.common.IDeviceAdapterListener;
import android.content.Context;

public interface IDeviceAdapterSeed {
    public IDeviceAdapter createDeviceAdapter(Context context, IDeviceAdapterListener listener);

    public ICameraManager createCameraManager();
}
