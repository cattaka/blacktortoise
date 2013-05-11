
package net.cattaka.android.ultimatetank.util;

import net.cattaka.android.ultimatetank.common.data.DeviceEventCode;
import net.cattaka.android.ultimatetank.common.data.DeviceState;
import net.cattaka.libgeppa.data.ConnectionCode;
import net.cattaka.libgeppa.data.ConnectionState;

public class DeviceUtil {
    public static DeviceState toDeviceState(ConnectionState state) {
        if (state == null) {
            return null;
        }
        switch (state) {
            case INITIAL:
                return DeviceState.INITIAL;
            case CONNECTING:
                return DeviceState.CONNECTING;
            case CONNECTED:
                return DeviceState.CONNECTED;
            case CLOSED:
                return DeviceState.CLOSED;
            case UNKNOWN:
            default:
                return DeviceState.UNKNOWN;
        }
    }

    public static DeviceEventCode toDeviceEventCode(ConnectionCode code) {
        if (code == null) {
            return null;
        }
        switch (code) {
            case DISCONNECTED:
                return DeviceEventCode.DISCONNECTED;
            case NO_DEVICE:
                return DeviceEventCode.NO_DEVICE;
            case UNKNOWN:
            default:
                return DeviceEventCode.UNKNOWN;
        }
    }

}
