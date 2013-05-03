
package net.cattaka.android.ultimatetank.usb;

import net.cattaka.android.ultimatetank.usb.data.MyPacket;
import net.cattaka.libgeppa.thread.ConnectionThread;

public interface ICommandAdapter {
    /**
     * @see ConnectionThread#sendPacket(net.cattaka.libgeppa.data.IPacket)
     */
    public boolean sendPacket(MyPacket packet);

    /**
     * @param leftMotor1 Input value 1 of left motor's driver
     * @param leftMotor2 Input value 2 of left motor's driver
     * @param rightMotor1 Input value 1 of right motor's driver
     * @param rightMotor2 Input value 2 of right motor's driver
     * @return If putting to queue is succeed it returns true, otherwise false.
     */
    public boolean sendMove(float leftMotor1, float leftMotor2, float rightMotor1, float rightMotor2);

    /**
     * @param yaw If 0 is given it turns left. If 1 is given it turns right.
     * @param pitch If 0 is given it turns up. If 1 is given it turns down.
     * @return If putting to queue is succeed it returns true, otherwise false.
     */
    public boolean sendHead(float yaw, float pitch);
}
