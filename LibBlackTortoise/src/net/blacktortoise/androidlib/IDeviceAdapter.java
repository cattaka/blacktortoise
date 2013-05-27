
package net.blacktortoise.androidlib;

import net.blacktortoise.androidlib.data.DeviceInfo;
import net.blacktortoise.androidlib.data.DeviceState;

public interface IDeviceAdapter extends IDeviceCommandAdapter {

    public void startAdapter() throws InterruptedException;

    public void stopAdapter() throws InterruptedException;

    public DeviceState getDeviceState();

    public DeviceInfo getDeviceInfo();
}
