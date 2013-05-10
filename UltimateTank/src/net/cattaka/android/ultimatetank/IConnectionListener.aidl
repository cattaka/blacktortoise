
package net.cattaka.android.ultimatetank;

import net.cattaka.libgeppa.data.ConnectionCode;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.android.ultimatetank.usb.data.MyPacket;

oneway interface IConnectionListener {
    void onConnectionStateChanged(in ConnectionState state, in ConnectionCode code);

    void onReceive(in MyPacket packet);
}
