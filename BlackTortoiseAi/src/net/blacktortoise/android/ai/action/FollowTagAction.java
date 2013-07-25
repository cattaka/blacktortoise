
package net.blacktortoise.android.ai.action;

import net.blacktortoise.android.ai.action.DetectTagAction.DetectTagArgs;
import net.blacktortoise.android.ai.action.HeadAction.HeadArgs;
import net.blacktortoise.android.ai.action.MoveAction.MoveArgs;
import net.blacktortoise.android.ai.action.TurnAction.TurnArgs;
import net.blacktortoise.android.ai.tagdetector.TagDetectResult;
import net.blacktortoise.android.ai.tagdetector.TagItem;
import net.blacktortoise.android.ai.util.BlackTortoiseServiceWrapperEx;
import net.blacktortoise.android.ai.util.PointUtil;

import org.opencv.core.Point;

import android.os.SystemClock;

public class FollowTagAction implements IAction<FollowTagAction.FollowTagArgs, TagDetectResult> {
    public static class FollowTagArgs {
        public final int tagKey;

        public final int time;

        public FollowTagArgs(int tagKey, int time) {
            super();
            this.tagKey = tagKey;
            this.time = time;
        }
    }

    private boolean finishOnCenter = false;

    private boolean enableMove = true;

    private boolean enableTurn = true;

    private DetectTagAction mDetectTagAction;

    private HeadAction mHeadAction;

    private MoveAction mMoveAction;

    private TurnAction mTurnAction;

    @Override
    public void setup(IActionUtil util) {
        mDetectTagAction = new DetectTagAction();
        mHeadAction = new HeadAction();
        mMoveAction = new MoveAction();
        mTurnAction = new TurnAction();
        mDetectTagAction.setup(util);
        mHeadAction.setup(util);
        mMoveAction.setup(util);
        mTurnAction.setup(util);
    }

    @Override
    public TagDetectResult execute(IActionUtil util, FollowTagArgs param)
            throws InterruptedException {
        BlackTortoiseServiceWrapperEx wrapper = util.getServiceWrapper();
        long t = SystemClock.elapsedRealtime();
        int width = util.getCapture().getWidth();
        int height = util.getCapture().getHeight();
        TagItem tagItem;
        {
            tagItem = util.getTagDetector().getTagItem(param.tagKey);
            if (tagItem == null) {
                return null;
            }
        }
        TagDetectResult result;
        Point p = new Point();
        do {
            result = mDetectTagAction.execute(util, new DetectTagArgs(param.tagKey, 100));
            if (result == null) {
                Thread.sleep(50);
                continue;
            }
            if ((SystemClock.elapsedRealtime() - t) > param.time) {
                break;
            }
            { // do single action
                if (enableTurn) {
                    PointUtil.getCenterScaled(p, result.getPoints(), width, height);
                    if (p.x < -0.25) {
                        mTurnAction.execute(util, new TurnArgs(-1f, 500));
                        continue;
                    } else if (p.x > 0.25) {
                        mTurnAction.execute(util, new TurnArgs(1f, 500));
                        continue;
                    }
                }
                if (enableMove) {
                    double scale = PointUtil.getAreaScaled(result.getPoints(), tagItem.getWidth(),
                            tagItem.getHeight());
                    if (scale < 0.8) {
                        mMoveAction.execute(util, new MoveArgs(1f, 500));
                        continue;
                    } else if (scale > 1.2) {
                        mMoveAction.execute(util, new MoveArgs(-1f, 500));
                        continue;
                    }
                }
                {
                    PointUtil.getCenterScaled(p, result.getPoints(), width, height);
                    if (p.x < -0.25) {
                        HeadArgs args = new HeadArgs(wrapper.getLastYaw() - 0.1f,
                                wrapper.getLastPitch());
                        mHeadAction.execute(util, args);
                        continue;
                    } else if (p.x > 0.25) {
                        HeadArgs args = new HeadArgs(wrapper.getLastYaw() + 0.1f,
                                wrapper.getLastPitch());
                        mHeadAction.execute(util, args);
                        continue;
                    }
                }
                if (finishOnCenter) {
                    break;
                }
            }
        } while (tagItem != null);

        return result;
    }

    public boolean isFinishOnCenter() {
        return finishOnCenter;
    }

    public void setFinishOnCenter(boolean finishOnCenter) {
        this.finishOnCenter = finishOnCenter;
    }

    public boolean isEnableMove() {
        return enableMove;
    }

    public void setEnableMove(boolean enableMove) {
        this.enableMove = enableMove;
    }

    public boolean isEnableTurn() {
        return enableTurn;
    }

    public void setEnableTurn(boolean enableTurn) {
        this.enableTurn = enableTurn;
    }

}