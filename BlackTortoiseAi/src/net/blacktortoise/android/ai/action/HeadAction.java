
package net.blacktortoise.android.ai.action;

public class HeadAction implements IAction<HeadAction.MoveArgs, Void> {
    public static class MoveArgs {
        public final float yaw;

        public final float pitch;

        public MoveArgs(float yaw, float pitch) {
            super();
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    @Override
    public void setup(IActionUtil util) {

    }

    @Override
    public Void execute(IActionUtil util, MoveArgs param) {
        util.getServiceWrapper().sendHead(param.yaw, param.pitch);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
