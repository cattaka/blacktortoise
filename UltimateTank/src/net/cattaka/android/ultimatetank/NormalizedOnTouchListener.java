
package net.cattaka.android.ultimatetank;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * This is wrapped listener class. This normalize the touch position between 0
 * and given maxValue.
 * 
 * @author cattaka
 */
abstract public class NormalizedOnTouchListener implements OnTouchListener {
    private int mMaxValue;

    public NormalizedOnTouchListener(int maxValue) {
        super();
        if (maxValue <= 0) {
            throw new IllegalArgumentException("maxValue must be more than 0");
        }
        mMaxValue = maxValue;
    }

    @Override
    public final boolean onTouch(View v, MotionEvent event) {
        int rx = (int)(mMaxValue * (event.getX()) / v.getWidth());
        int ry = (int)(mMaxValue * (event.getY()) / v.getHeight());

        rx = Math.max(0, Math.min(mMaxValue, rx));
        ry = Math.max(0, Math.min(mMaxValue, ry));

        return onTouch(v, event, rx, ry);
    }

    /**
     * This is wrapped listener function.
     * 
     * @param v same as the original
     * @param event same as the original
     * @param rx Normalized x position between 0 and maxValue.
     * @param ry Normalized x position between 0 and maxValue.
     * @return
     */
    abstract public boolean onTouch(View v, MotionEvent event, int rx, int ry);
}
