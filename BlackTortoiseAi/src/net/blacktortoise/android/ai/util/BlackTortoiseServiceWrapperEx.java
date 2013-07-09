
package net.blacktortoise.android.ai.util;

import net.blacktortoise.androidlib.BlackTortoiseServiceWrapper;
import net.blacktortoise.androidlib.IBlackTortoiseService;

public class BlackTortoiseServiceWrapperEx extends BlackTortoiseServiceWrapper {
    private float mLastForward;

    private float mLastTurn;

    private float mLastYaw;

    private float mLastPitch;

    public BlackTortoiseServiceWrapperEx(IBlackTortoiseService service) {
        super(service);
    }

    @Override
    public boolean sendMove(float forward, float turn) {
        return super.sendMove(forward, turn);
    }

    @Override
    public boolean sendHead(float yaw, float pitch) {
        return super.sendHead(yaw, pitch);
    }

    public float getLastForward() {
        return mLastForward;
    }

    public float getLastTurn() {
        return mLastTurn;
    }

    public float getLastYaw() {
        return mLastYaw;
    }

    public float getLastPitch() {
        return mLastPitch;
    }

}
