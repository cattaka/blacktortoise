
package net.cattaka.android.blacktortoise;

import net.cattaka.android.blacktortoise.common.data.DeviceEventCode;
import net.cattaka.android.blacktortoise.common.data.DeviceState;
import net.cattaka.android.blacktortoise.common.data.BtPacket;

oneway interface IBlackTortoiseServiceListener {
    void onDeviceStateChanged(in DeviceState state, in DeviceEventCode code);

    void onReceive(in BtPacket packet);
}
