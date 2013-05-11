
package net.cattaka.android.ultimatetank;

import net.cattaka.android.ultimatetank.common.data.DeviceEventCode;
import net.cattaka.android.ultimatetank.common.data.DeviceState;
import net.cattaka.android.ultimatetank.common.data.BtPacket;

oneway interface IBlackTortoiseServiceListener {
    void onDeviceStateChanged(in DeviceState state, in DeviceEventCode code);

    void onReceive(in BtPacket packet);
}
