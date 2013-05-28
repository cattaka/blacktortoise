
package net.blacktortoise.androidlib;

import net.blacktortoise.androidlib.data.BtPacket;
import net.cattaka.libgeppa.thread.ConnectionThread;

public interface IDeviceCommandAdapter {

    /**
     * @see ConnectionThread#sendPacket(net.cattaka.libgeppa.data.IPacket)
     */
    public boolean sendPacket(BtPacket packet);

    /**
     * @return If putting to queue is succeed it returns true, otherwise false.
     */
    public boolean sendRequestCameraImage();

    public boolean isCameraSupported();
}
