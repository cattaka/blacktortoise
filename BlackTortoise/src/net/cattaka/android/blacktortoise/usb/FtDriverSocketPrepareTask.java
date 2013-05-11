
package net.cattaka.android.blacktortoise.usb;

import jp.ksksue.driver.serial.FTDriver;
import net.cattaka.libgeppa.IRawSocket;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class FtDriverSocketPrepareTask implements IRawSocketPrepareTask {
    private UsbManager mUsbManager;

    private UsbDevice mUsbDevice;

    public FtDriverSocketPrepareTask(UsbManager usbManager, UsbDevice usbDevice) {
        super();
        mUsbManager = usbManager;
        mUsbDevice = usbDevice;
    }

    @Override
    public IRawSocket prepareRawSocket() {
        FTDriver mFtDriver = new FTDriver(mUsbManager);
        mFtDriver.setRxTimeout(1000); // Is this effective?.
        if (mFtDriver.begin(FTDriver.BAUD115200, mUsbDevice)) {
            return new FtDriverSocket(mFtDriver);
        } else {
            return null;
        }
    }

}
