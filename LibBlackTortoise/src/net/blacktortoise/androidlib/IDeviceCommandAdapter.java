
package net.blacktortoise.androidlib;

import net.blacktortoise.androidlib.data.BtPacket;
import net.cattaka.libgeppa.thread.ConnectionThread;

public interface IDeviceCommandAdapter {

    /**
     * @see ConnectionThread#sendPacket(net.cattaka.libgeppa.data.IPacket)
     */
    public boolean sendPacket(BtPacket packet);

    public boolean isCameraSupported();
}
