
package net.cattaka.android.ultimatetank.seed;

import net.cattaka.android.ultimatetank.camera.ICameraManager;
import net.cattaka.android.ultimatetank.common.IDeviceAdapter;
import net.cattaka.android.ultimatetank.common.IDeviceAdapterListener;
import android.content.Context;

public interface IDeviceAdapterSeed {
    public IDeviceAdapter createDeviceAdapter(Context context, IDeviceAdapterListener listener);

    public ICameraManager createCameraManager();
}
