
package net.blacktortoise.androidlib;

import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.DeviceEventCode;
import net.blacktortoise.androidlib.data.DeviceInfo;
import net.blacktortoise.androidlib.data.DeviceState;
import android.graphics.Bitmap;

public interface IDeviceAdapterListener {

    void onDeviceStateChanged(DeviceState state, DeviceEventCode code, DeviceInfo deviceInfo);

    void onReceive(BtPacket packet);

    void onReceiveCameraImage(int cameraIdx, Bitmap bitmap);
}
