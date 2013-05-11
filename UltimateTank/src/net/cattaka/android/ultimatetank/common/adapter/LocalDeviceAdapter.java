
package net.cattaka.android.ultimatetank.common.adapter;

import net.cattaka.android.ultimatetank.common.IDeviceAdapterListener;
import net.cattaka.android.ultimatetank.usb.FtDriverSocketPrepareTask;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;
import android.hardware.usb.UsbDevice;

public class LocalDeviceAdapter extends BtConnectionAdapter {
    private UsbDevice mUsbDevice;

    public LocalDeviceAdapter(IDeviceAdapterListener listener, boolean useMainLooperForListener,
            UsbDevice usbDevice) {
        super(listener, useMainLooperForListener);
        mUsbDevice = usbDevice;
    }

    @Override
    protected IRawSocketPrepareTask createRawSocketPrepareTask() {
        return new FtDriverSocketPrepareTask(mUsbDevice);
    }
}
