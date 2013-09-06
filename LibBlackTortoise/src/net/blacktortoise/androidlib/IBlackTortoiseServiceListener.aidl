
package net.blacktortoise.androidlib;

import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceState;
import net.cattaka.libgeppa.data.PacketWrapper;
import net.cattaka.libgeppa.data.DeviceInfo;

oneway interface IBlackTortoiseServiceListener {
    void onDeviceStateChanged(in DeviceState state, in DeviceEventCode code, in DeviceInfo deviceInfo);

    void onReceivePacket(in PacketWrapper packet);
}
