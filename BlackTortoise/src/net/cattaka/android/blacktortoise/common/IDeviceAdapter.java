
package net.cattaka.android.blacktortoise.common;

import net.cattaka.android.blacktortoise.common.data.DeviceState;


public interface IDeviceAdapter extends IDeviceCommandAdapter {

    public void startAdapter() throws InterruptedException;

    public void stopAdapter() throws InterruptedException;

    public DeviceState getDeviceState();
}
