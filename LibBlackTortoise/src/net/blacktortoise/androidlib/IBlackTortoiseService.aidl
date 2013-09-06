
package net.blacktortoise.androidlib;

import net.blacktortoise.androidlib.IBlackTortoiseServiceListener;
import net.cattaka.libgeppa.data.PacketWrapper;
import net.cattaka.libgeppa.data.DeviceInfo;

interface IBlackTortoiseService {

    int registerServiceListener(IBlackTortoiseServiceListener listener);

    void unregisterServiceListener(int seq);

    void connect(in DeviceInfo deviceInfo);

    void disconnect();

    DeviceInfo getCurrentDeviceInfo();

    /**
     * @see ConnectionThread#sendPacket(net.cattaka.libgeppa.data.IPacket)
     */
    boolean sendPacket(in PacketWrapper packet);
}
