
package net.cattaka.android.ultimatetank.usb;

import net.cattaka.android.ultimatetank.camera.DeviceCameraManager;
import net.cattaka.android.ultimatetank.camera.ICameraManager;
import net.cattaka.libgeppa.IRawSocket;
import android.content.Context;

public class DummySocketPrepareTask implements IMySocketPrepareTask {
    public DummySocketPrepareTask() {
        super();
        // none
    }

    public void setup(Context context) {
        // none
    }

    @Override
    public IRawSocket prepareRawSocket() {
        return new DummySocket();
    }

    @Override
    public ICameraManager createCameraManager() {
        return new DeviceCameraManager();
    }
}
