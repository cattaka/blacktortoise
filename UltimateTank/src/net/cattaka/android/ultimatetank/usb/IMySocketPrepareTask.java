
package net.cattaka.android.ultimatetank.usb;

import net.cattaka.android.ultimatetank.camera.ICameraManager;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;

public interface IMySocketPrepareTask extends IRawSocketPrepareTask {
    public ICameraManager createCameraManager();
}
