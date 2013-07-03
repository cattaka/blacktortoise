
package net.blacktortoise.android.ai.tagdetector;

import java.util.ArrayList;
import java.util.List;

import net.blacktortoise.android.ai.util.WorkCaches;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;

import android.util.SparseArray;

public class TagDetector {
    private SparseArray<TagItem> mTagItems;

    private WorkCaches mWorkCaches;

    private FeatureDetector mDetector;

    private DescriptorExtractor mExtractor;

    private DescriptorMatcher mMatcher;

    public TagDetector() {
        mTagItems = new SparseArray<TagItem>();
        mWorkCaches = new WorkCaches();
        // detector = FeatureDetector.create(FeatureDetector.ORB);
        // extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        // matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        mDetector = FeatureDetector.create(FeatureDetector.BRISK);
        mExtractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
        mMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
    }

    public void putTagItem(int key, TagItem tagItem) {
        mTagItems.put(key, tagItem);
    }

    public TagItem getTagItem(int key) {
        return mTagItems.get(key);
    }

    public void removeTagItem(int key) {
        mTagItems.remove(key);
        ;
    }

    public FeatureDetector getDetector() {
        return mDetector;
    }

    public DescriptorExtractor getExtractor() {
        return mExtractor;
    }

    public DescriptorMatcher getMatcher() {
        return mMatcher;
    }

    public void upgradeTagItem(TagItem target, Mat src) {
        MatOfKeyPoint mokp = new MatOfKeyPoint();
        Mat queryDescriptors = new Mat();
        mDetector.detect(src, mokp);
        mExtractor.compute(src, mokp, queryDescriptors);
        target.addFrame(queryDescriptors, mokp.toArray());
    }

    public void detectTags(List<TagDetectResult> dst, Mat src, Mat resultMat) {
        KeyPoint[] keypoints;
        Mat descriptors = mWorkCaches.getWorkMat(4);
        { // Detect and Extract keypoints
            MatOfKeyPoint mokp = new MatOfKeyPoint();
            mDetector.detect(src, mokp);
            mExtractor.compute(src, mokp, descriptors);
            keypoints = mokp.toArray();
        }

        if (resultMat != null) {
            { // Draw Keypoints
                Scalar color = new Scalar(0xFF, 0xFF, 0xFF, 0xFF);
                for (KeyPoint kp : keypoints) {
                    Core.circle(resultMat, kp.pt, 10, color, 1);
                }
            }
        }

        for (int j = 0; j < mTagItems.size(); j++) {
            TagItem tagItem = mTagItems.valueAt(j);
            for (int k = 0; k < tagItem.getCount(); k++) {
                List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
                Mat queryDescriptors = tagItem.getDescriptors(k);
                KeyPoint[] queryKeyPoints = tagItem.getKeyPoints(k);
                Mat trainDescriptors = descriptors;
                mMatcher.knnMatch(queryDescriptors, trainDescriptors, matches, 2);

                List<DMatch> good_matches = new ArrayList<DMatch>();
                for (MatOfDMatch mm : matches) {
                    DMatch[] m = mm.toArray();
                    if (m.length >= 2 && (m[0].distance < 0.6 * m[1].distance)) {
                        good_matches.add(m[0]);
                    }
                }
                MatOfPoint2f dstMop = null;
                float maxDistance = 1;
                if (good_matches.size() >= 4) {
                    { // calculate maxDistance
                        for (MatOfDMatch mm : matches) {
                            for (DMatch m : mm.toArray()) {
                                if (maxDistance < m.distance) {
                                    maxDistance = m.distance;
                                }
                            }
                        }
                    }

                    Mat homography;
                    { // calculate homography
                        MatOfPoint2f srcPoints;
                        MatOfPoint2f dstPoints;
                        { // Pickup good matches to calculate homography
                            List<Point> srcPointList = new ArrayList<Point>();
                            List<Point> dstPointList = new ArrayList<Point>();
                            for (DMatch m : good_matches) {
                                srcPointList.add(queryKeyPoints[m.queryIdx].pt);
                                dstPointList.add(keypoints[m.trainIdx].pt);
                            }
                            srcPoints = new MatOfPoint2f(
                                    srcPointList.toArray(new Point[srcPointList.size()]));
                            dstPoints = new MatOfPoint2f(
                                    dstPointList.toArray(new Point[dstPointList.size()]));
                        }
                        homography = Calib3d
                                .findHomography(srcPoints, dstPoints, Calib3d.RANSAC, 3);
                    }
                    Point[] srcPt = new Point[4];
                    for (int i = 0; i < srcPt.length; i++) {
                        srcPt[i] = new Point();
                    }
                    MatOfPoint2f srcMop = new MatOfPoint2f( //
                            new Point(0, 0), //
                            new Point(tagItem.getWidth(), 0), //
                            new Point(tagItem.getWidth(), tagItem.getHeight()), //
                            new Point(0, tagItem.getHeight()));
                    dstMop = new MatOfPoint2f(srcPt);
                    Core.perspectiveTransform(srcMop, dstMop, homography);
                }
                if (resultMat != null) {
                    if (dstMop != null) { // Draw line
                        Point[] dstPt = new Point[4];
                        dstPt = dstMop.toArray();
                        Scalar color = new Scalar(0xFF, 0xFF, 0, 0);
                        Core.line(resultMat, dstPt[0], dstPt[1], color);
                        Core.line(resultMat, dstPt[1], dstPt[2], color);
                        Core.line(resultMat, dstPt[2], dstPt[3], color);
                        Core.line(resultMat, dstPt[3], dstPt[0], color);
                    }
                    { // Draw good point
                        for (DMatch m : good_matches) {
                            KeyPoint kp = keypoints[m.trainIdx];
                            Scalar color = new Scalar(0xFF * m.distance / maxDistance, 0xFF, 0, 0);
                            Core.circle(resultMat, kp.pt, 10, color, -1);
                        }
                    }
                }
            }
        }
    }
}
