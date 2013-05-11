
package net.cattaka.android.ultimatetank.common;

import net.cattaka.android.ultimatetank.common.data.DeviceEventCode;
import net.cattaka.android.ultimatetank.common.data.DeviceState;
import android.graphics.Bitmap;

public interface IDeviceAdapterListener {

    void onDeviceStateChanged(DeviceState state, DeviceEventCode code);

    void onReceiveEcho(byte[] data);

    void onReceiveCameraImage(int cameraIdx, Bitmap bitmap);
}
