
package net.blacktortoise.android.ai.action;

public class TurnAction implements IAction<TurnAction.TurnArgs, Void> {
    public static class TurnArgs {
        public final float turn;

        public final int time;

        public TurnArgs(float turn, int time) {
            super();
            this.turn = turn;
            this.time = time;
        }

    }

    @Override
    public void setup(IActionUtil util) {

    }

    @Override
    public Void execute(IActionUtil util, TurnArgs param) {
        if (param.turn > 0) {
            util.getServiceWrapper().sendMove(param.turn, 1);
        } else {
            util.getServiceWrapper().sendMove(param.turn, -1);
        }
        try {
            Thread.sleep(param.time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            util.getServiceWrapper().sendMove(0, 0);
        }

        return null;
    }
}
