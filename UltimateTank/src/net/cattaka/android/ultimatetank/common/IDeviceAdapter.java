
package net.cattaka.android.ultimatetank.common;

import net.cattaka.android.ultimatetank.common.data.DeviceState;


public interface IDeviceAdapter extends IDeviceCommandAdapter {

    public void startAdapter() throws InterruptedException;

    public void stopAdapter() throws InterruptedException;

    public DeviceState getDeviceState();
}
