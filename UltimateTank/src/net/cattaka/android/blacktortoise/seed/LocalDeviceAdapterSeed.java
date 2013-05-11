
package net.cattaka.android.blacktortoise.seed;

import net.cattaka.android.blacktortoise.camera.DeviceCameraManager;
import net.cattaka.android.blacktortoise.camera.ICameraManager;
import net.cattaka.android.blacktortoise.common.IDeviceAdapter;
import net.cattaka.android.blacktortoise.common.IDeviceAdapterListener;
import net.cattaka.android.blacktortoise.common.adapter.LocalDeviceAdapter;
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
