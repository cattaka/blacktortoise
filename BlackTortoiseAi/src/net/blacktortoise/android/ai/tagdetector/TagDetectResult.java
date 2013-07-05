
package net.blacktortoise.android.ai.tagdetector;

import org.opencv.core.Point;

public class TagDetectResult {
    private int mTagKey;

    private Point[] mPoints;

    public TagDetectResult() {
        super();
    }

    public TagDetectResult(int tagKey, Point[] points) {
        super();
        mTagKey = tagKey;
        mPoints = points;
    }

    public int getTagKey() {
        return mTagKey;
    }

    public void setTagKey(int tagKey) {
        mTagKey = tagKey;
    }

    public Point[] getPoints() {
        return mPoints;
    }

    public void setPoints(Point[] points) {
        mPoints = points;
    }

}
