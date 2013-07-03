
package net.blacktortoise.android.ai.tagdetector;

import org.opencv.core.Mat;
import org.opencv.features2d.KeyPoint;

public class TagItem {
    private int width;

    private int height;

    private Mat queryDescriptors;

    private KeyPoint[] queryKeyPoints;

    public TagItem(int width, int height, Mat queryDescriptors, KeyPoint[] queryKeyPoints) {
        super();
        this.width = width;
        this.height = height;
        this.queryDescriptors = queryDescriptors;
        this.queryKeyPoints = queryKeyPoints;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Mat getQueryDescriptors() {
        return queryDescriptors;
    }

    public void setQueryDescriptors(Mat queryDescriptors) {
        this.queryDescriptors = queryDescriptors;
    }

    public KeyPoint[] getQueryKeyPoints() {
        return queryKeyPoints;
    }

    public void setQueryKeyPoints(KeyPoint[] queryKeyPoints) {
        this.queryKeyPoints = queryKeyPoints;
    }
}
