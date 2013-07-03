
package net.blacktortoise.android.ai.tagdetector;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.features2d.KeyPoint;

public class TagItem {
    private int width;

    private int height;

    private List<Mat> descriptorsList;

    private List<KeyPoint[]> keyPointsList;

    public TagItem(int width, int height) {
        this.width = width;
        this.height = height;
        this.descriptorsList = new ArrayList<Mat>();
        this.keyPointsList = new ArrayList<KeyPoint[]>();
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

    public Mat getDescriptors(int idx) {
        return descriptorsList.get(idx);
    }

    public KeyPoint[] getKeyPoints(int idx) {
        return keyPointsList.get(idx);
    }

    public int getCount() {
        return descriptorsList.size();
    }

    public void addFrame(Mat mat, KeyPoint[] kps) {
        descriptorsList.add(mat);
        keyPointsList.add(kps);
    }
}
