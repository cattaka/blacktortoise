
package net.blacktortoise.android.common;

import net.blacktortoise.android.common.data.DeviceEventCode;
import net.blacktortoise.android.common.data.DeviceState;
import android.graphics.Bitmap;

public interface IDeviceAdapterListener {

    void onDeviceStateChanged(DeviceState state, DeviceEventCode code);

    void onReceiveEcho(byte[] data);

    void onReceiveCameraImage(int cameraIdx, Bitmap bitmap);
}
