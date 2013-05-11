
package net.blacktortoise.android.common.adapter;

import net.blacktortoise.android.common.IDeviceAdapterListener;
import net.blacktortoise.android.usb.FtDriverSocketPrepareTask;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class LocalDeviceAdapter extends BtConnectionAdapter {
    private UsbManager mUsbManager;

    private UsbDevice mUsbDevice;

    public LocalDeviceAdapter(IDeviceAdapterListener listener, boolean useMainLooperForListener,
            UsbManager usbManager, UsbDevice usbDevice) {
        super(listener, useMainLooperForListener);
        mUsbManager = usbManager;
        mUsbDevice = usbDevice;
    }

    @Override
    protected IRawSocketPrepareTask createRawSocketPrepareTask() {
        return new FtDriverSocketPrepareTask(mUsbManager, mUsbDevice);
    }
}
