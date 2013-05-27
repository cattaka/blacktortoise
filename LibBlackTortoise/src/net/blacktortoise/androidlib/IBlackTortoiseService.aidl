
package net.blacktortoise.androidlib;

import net.blacktortoise.androidlib.IBlackTortoiseServiceListener;
import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.DeviceInfo;

interface IBlackTortoiseService {

    int registerServiceListener(IBlackTortoiseServiceListener listener);

    void unregisterServiceListener(int seq);

    void connect(in DeviceInfo deviceInfo);

    void disconnect();

    DeviceInfo getCurrentDeviceInfo();

    /**
     * @see ConnectionThread#sendPacket(net.cattaka.libgeppa.data.IPacket)
     */
    boolean sendPacket(in BtPacket packet);

    /**
     * @param leftMotor1 Input value 1 of left motor's driver
     * @param leftMotor2 Input value 2 of left motor's driver
     * @param rightMotor1 Input value 1 of right motor's driver
     * @param rightMotor2 Input value 2 of right motor's driver
     * @return If putting to queue is succeed it returns true, otherwise false.
     */
    boolean sendMove(in float leftMotor1, in float leftMotor2, in float rightMotor1, in float rightMotor2);

    /**
     * @param yaw If 0 is given it turns left. If 1 is given it turns right.
     * @param pitch If 0 is given it turns up. If 1 is given it turns down.
     * @return If putting to queue is succeed it returns true, otherwise false.
     */
    boolean sendHead(in float yaw, in float pitch);


    /**
     * @param data date for echo.
     * @return If putting to queue is succeed it returns true, otherwise false.
     */
    boolean sendEcho(in byte[] data);

    /**
     * @return If putting to queue is succeed it returns true, otherwise false.
     */
    boolean requestCameraImage();
}
