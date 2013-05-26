
package net.blacktortoise.android.seed;

import net.blacktortoise.android.camera.DeviceCameraManager;
import net.blacktortoise.android.camera.ICameraManager;
import net.blacktortoise.androidlib.IDeviceAdapter;
import net.blacktortoise.androidlib.IDeviceAdapterListener;
import net.blacktortoise.androidlib.adapter.LocalDeviceAdapter;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class LocalDeviceAdapterSeed implements IDeviceAdapterSeed {
    private UsbDevice mUsbDevice;

    public LocalDeviceAdapterSeed(UsbDevice usbDevice) {
        super();
        mUsbDevice = usbDevice;
    }

    @Override
    public IDeviceAdapter createDeviceAdapter(Context context, IDeviceAdapterListener listener) {
        UsbManager usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
        LocalDeviceAdapter adapter = new LocalDeviceAdapter(listener, true, usbManager, mUsbDevice);
        return adapter;
    }

    @Override
    public ICameraManager createCameraManager() {
        return new DeviceCameraManager();
    }
}
