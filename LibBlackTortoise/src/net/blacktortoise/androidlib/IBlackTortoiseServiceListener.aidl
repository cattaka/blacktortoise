
package net.blacktortoise.androidlib;

import net.blacktortoise.androidlib.data.DeviceEventCode;
import net.blacktortoise.androidlib.data.DeviceState;
import net.blacktortoise.androidlib.data.BtPacket;

oneway interface IBlackTortoiseServiceListener {
    void onDeviceStateChanged(in DeviceState state, in DeviceEventCode code, in String deviceKey);

    void onReceive(in BtPacket packet);
}
