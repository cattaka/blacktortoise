
package net.cattaka.android.ultimatetank.usb;

import jp.ksksue.driver.serial.FTDriver;
import net.cattaka.android.ultimatetank.camera.DeviceCameraManager;
import net.cattaka.android.ultimatetank.camera.ICameraManager;
import net.cattaka.libgeppa.IRawSocket;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class FtDriverSocketPrepareTask implements IMySocketPrepareTask {
    private UsbManager mUsbManager;

    private UsbDevice mUsbDevice;

    public FtDriverSocketPrepareTask(UsbDevice usbDevice) {
        super();
        mUsbDevice = usbDevice;
    }

    public void setup(Context context) {
        mUsbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
    }

    @Override
    public IRawSocket prepareRawSocket() {
        FTDriver mFtDriver = new FTDriver(mUsbManager);
        mFtDriver.setRxTimeout(1000); // This is not effective.
        if (mFtDriver.begin(FTDriver.BAUD115200, mUsbDevice)) {
            return new FtDriverSocket(mFtDriver);
        } else {
            return null;
        }
    }

    @Override
    public ICameraManager createCameraManager() {
        return new DeviceCameraManager();
    }

}
