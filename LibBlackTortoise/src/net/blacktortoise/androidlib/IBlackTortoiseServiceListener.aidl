
package net.blacktortoise.androidlib;

import net.blacktortoise.androidlib.data.DeviceEventCode;
import net.blacktortoise.androidlib.data.DeviceState;
import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.DeviceInfo;

oneway interface IBlackTortoiseServiceListener {
    void onDeviceStateChanged(in DeviceState state, in DeviceEventCode code, in DeviceInfo deviceInfo);

    void onReceive(in BtPacket packet);
}
