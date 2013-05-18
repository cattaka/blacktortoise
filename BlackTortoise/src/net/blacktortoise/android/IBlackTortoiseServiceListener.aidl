
package net.blacktortoise.android;

import net.blacktortoise.android.common.data.DeviceEventCode;
import net.blacktortoise.android.common.data.DeviceState;
import net.blacktortoise.android.common.data.BtPacket;

oneway interface IBlackTortoiseServiceListener {
    void onDeviceStateChanged(in DeviceState state, in DeviceEventCode code, in String deviceKey);

    void onReceive(in BtPacket packet);
}
