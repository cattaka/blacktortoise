
package net.cattaka.android.ultimatetank.usb;

import net.cattaka.android.ultimatetank.net.data.MyPacket;
import net.cattaka.libgeppa.thread.ConnectionThread;

public interface ICommandAdapter {
    /**
     * @see ConnectionThread#sendPacket(net.cattaka.libgeppa.data.IPacket)
     */
    public boolean sendPacket(MyPacket packet);

    /**
     * @param forward If 0 is given it moves forward. If 1 is given it moves
     *            forward.
     * @param side If 0 is given it turn left. If 1 is given it moves right.
     * @return If putting to queue is succeed it returns true, otherwise false.
     */
    public boolean sendMove(float forward, float side);

    /**
     * @param yaw If 0 is given it turns left. If 1 is given it turns right.
     * @param pitch If 0 is given it turns up. If 1 is given it turns down.
     * @return If putting to queue is succeed it returns true, otherwise false.
     */
    public boolean sendHead(float yaw, float pitch);
}
