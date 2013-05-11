
package net.cattaka.android.ultimatetank.util;

import net.cattaka.android.ultimatetank.common.IDeviceCommandAdapter;

public class CommandAdapterUtil {
    /**
     * @param adapter
     * @param forward Value for forward. This must be between -1 and 1. When
     *            value is positive it moves forward, when value is negative it
     *            moves backward.
     * @param turn Value for turn. This must be between -1 and 1. When value is
     *            positive it turns right, when value is negative it turns left.
     */
    public static void sendMove(IDeviceCommandAdapter adapter, float forward, float turn) {
        float leftMotor = Math.min(1, 1 + turn * 2) * forward;
        float rightMotor = Math.min(1, 1 - turn * 2) * forward;

        float leftMotor1 = (leftMotor > 0) ? leftMotor : 0;
        float leftMotor2 = (leftMotor < 0) ? -leftMotor : 0;
        float rightMotor1 = (rightMotor > 0) ? rightMotor : 0;
        float rightMotor2 = (rightMotor < 0) ? -rightMotor : 0;

        adapter.sendMove(leftMotor1, leftMotor2, rightMotor1, rightMotor2);
    }
}
