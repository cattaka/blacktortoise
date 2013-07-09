
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

        dst.clear();
        for (int j = 0; j < mTagItems.size(); j++) {
            int tagKey = mTagItems.keyAt(j);
            Mat trainDescriptors = descriptors;
            TagDetectResult result = detectTagInner(src, resultMat, tagKey, trainDescriptors,
                    keypoints);
            if (result != null) {
                dst.add(result);
            }
        }
    }

    public TagDetectResult detectTag(Mat src, Mat resultMat, int tagKey) {
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

        Mat trainDescriptors = descriptors;
        return detectTagInner(src, resultMat, tagKey, trainDescriptors, keypoints);
    }

    public TagDetectResult detectTagInner(Mat src, Mat resultMat, int tagKey, Mat trainDescriptors,
            KeyPoint[] keypoints) {
        TagItem tagItem = mTagItems.get(tagKey);
        List<Point[]> goodPts = new ArrayList<Point[]>();
        for (int k = 0; k < tagItem.getCount(); k++) {
            List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
            Mat queryDescriptors = tagItem.getDescriptors(k);
            KeyPoint[] queryKeyPoints = tagItem.getKeyPoints(k);
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
                        srcPoints = new MatOfPoint2f(srcPointList.toArray(new Point[srcPointList
                                .size()]));
                        dstPoints = new MatOfPoint2f(dstPointList.toArray(new Point[dstPointList
                                .size()]));
                    }
                    homography = Calib3d.findHomography(srcPoints, dstPoints, Calib3d.RANSAC, 3);
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
                    boolean valid = isValid(tagItem.getWidth(), tagItem.getHeight(), dstPt);
                    if (valid) {
                        goodPts.add(dstPt);
                    }
                    // Scalar color = (valid) ? new Scalar(0xFF, 0xFF, 0, 0)
                    // : new Scalar(0xFF, 0,
                    // 0, 0xFF);
                    // Core.line(resultMat, dstPt[0], dstPt[1], color);
                    // Core.line(resultMat, dstPt[1], dstPt[2], color);
                    // Core.line(resultMat, dstPt[2], dstPt[3], color);
                    // Core.line(resultMat, dstPt[3], dstPt[0], color);
                }
                { // Draw good point
                  // for (DMatch m : good_matches) {
                  // KeyPoint kp = keypoints[m.trainIdx];
                  // Scalar color = new Scalar(0xFF * m.distance /
                  // maxDistance, 0xFF, 0, 0);
                  // Core.circle(resultMat, kp.pt, 10, color, -1);
                  // }
                }
            }
        }
        if (goodPts.size() > 2) {
            Point[] pts = new Point[] {
                    new Point(), new Point(), new Point(), new Point()
            };
            calcAverage(pts, goodPts);
            TagDetectResult result = new TagDetectResult(tagKey, pts);
            if (resultMat != null) {
                Scalar color = new Scalar(0xFF, 0xFF, 0, 0);
                Core.line(resultMat, pts[0], pts[1], color);
                Core.line(resultMat, pts[1], pts[2], color);
                Core.line(resultMat, pts[2], pts[3], color);
                Core.line(resultMat, pts[3], pts[0], color);
            }
            return result;
        } else {
            return null;
        }
    }

    private void calcAverage(Point[] dst, List<Point[]> pts) {
        dst[0].x = 0;
        dst[0].y = 0;
        dst[1].x = 0;
        dst[1].y = 0;
        dst[2].x = 0;
        dst[2].y = 0;
        dst[3].x = 0;
        dst[3].y = 0;
        for (Point[] pt : pts) {
            dst[0].x += pt[0].x;
            dst[0].y += pt[0].y;
            dst[1].x += pt[1].x;
            dst[1].y += pt[1].y;
            dst[2].x += pt[2].x;
            dst[2].y += pt[2].y;
            dst[3].x += pt[3].x;
            dst[3].y += pt[3].y;
        }
        int n = pts.size();
        dst[0].x /= n;
        dst[0].y /= n;
        dst[1].x /= n;
        dst[1].y /= n;
        dst[2].x /= n;
        dst[2].y /= n;
        dst[3].x /= n;
        dst[3].y /= n;

    }

    public boolean isValid(double width, double height, Point[] ps) {
        double minSquare = width * height * 0.5 * 0.5;
        double maxSquare = width * height * 1.5 * 1.5;

        double x01 = ps[0].x - ps[1].x;
        double y01 = ps[0].y - ps[1].y;
        double x03 = ps[0].x - ps[3].x;
        double y03 = ps[0].y - ps[3].y;
        double x21 = ps[2].x - ps[1].x;
        double y21 = ps[2].y - ps[1].y;
        double x23 = ps[2].x - ps[3].x;
        double y23 = ps[2].y - ps[3].y;

        double s = -(x03 * y01 - x01 * y03 + x21 * y23 - x23 * y21) / 2;

        return (minSquare <= s && s <= maxSquare);
    }
}
