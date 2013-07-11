
package net.blacktortoise.android.ai.action;

public class HeadAction implements IAction<HeadAction.HeadArgs, Void> {
    public static class HeadArgs {
        public final float yaw;

        public final float pitch;

        public HeadArgs(float yaw, float pitch) {
            super();
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    @Override
    public void setup(IActionUtil util) {

    }

    @Override
    public Void execute(IActionUtil util, HeadArgs param) {
        util.getServiceWrapper().sendHead(param.yaw, param.pitch);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
