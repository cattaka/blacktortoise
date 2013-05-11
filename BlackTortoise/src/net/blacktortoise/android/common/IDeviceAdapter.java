
package net.blacktortoise.android.common;

import net.blacktortoise.android.common.data.DeviceState;


public interface IDeviceAdapter extends IDeviceCommandAdapter {

    public void startAdapter() throws InterruptedException;

    public void stopAdapter() throws InterruptedException;

    public DeviceState getDeviceState();
}
