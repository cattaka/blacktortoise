
package net.blacktortoise.androidlib;

import net.blacktortoise.androidlib.data.DeviceEventCode;
import net.blacktortoise.androidlib.data.DeviceInfo;
import net.blacktortoise.androidlib.data.DeviceState;
import android.graphics.Bitmap;

public interface IDeviceAdapterListener {

    void onDeviceStateChanged(DeviceState state, DeviceEventCode code, DeviceInfo deviceInfo);

    void onReceiveEcho(byte[] data);

    void onReceiveCameraImage(int cameraIdx, Bitmap bitmap);
}
