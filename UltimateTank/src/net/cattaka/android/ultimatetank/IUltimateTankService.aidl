
package net.cattaka.android.ultimatetank;

import net.cattaka.android.ultimatetank.IConnectionListener;
import net.cattaka.android.ultimatetank.usb.data.MyPacket;

interface IUltimateTankService {

    int registerConnectionListener(IConnectionListener listener);

    void unregisterConnectionListener(int seq);

    void connect(in String deviceKey);

    void disconnect();

    /**
     * @see ConnectionThread#sendPacket(net.cattaka.libgeppa.data.IPacket)
     */
    boolean sendPacket(in MyPacket packet);

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
     * @return If putting to queue is succeed it returns true, otherwise false.
     */
    boolean requestCameraImage();
}
