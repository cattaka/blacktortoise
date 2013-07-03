
package net.blacktortoise.android.ai;

import java.util.ArrayList;
import java.util.List;

import net.blacktortoise.android.ai.tagdetector.TagItem;
import net.blacktortoise.android.ai.util.ImageUtil;
import net.blacktortoise.android.ai.util.WorkCaches;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {
    private static final int EVENT_CAPTURE = 1;

    private WorkCaches mWorkCaches;

    private VideoCapture mCapture;

    private ImageView mCaptureImageView;

    private static Handler sHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            MainActivity target = (MainActivity)msg.obj;
            if (msg.what == EVENT_CAPTURE) {
                target.updateCapture();
                obtainMessage(EVENT_CAPTURE, target).sendToTarget();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWorkCaches = new WorkCaches();

        mCaptureImageView = (ImageView)findViewById(R.id.captureImageView);
        mCaptureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTagDetector != null) {
                    mTagDetector.resetMatch = true;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
                new LoaderCallbackInterface() {
                    @Override
                    public void onPackageInstall(int operation, InstallCallbackInterface callback) {

                    }

                    @Override
                    public void onManagerConnected(int status) {
                        if (status == LoaderCallbackInterface.SUCCESS) {
                            mTagDetector = new TagDetector();
                            mCapture = new VideoCapture();
                            mCapture.open(0);
                            List<Size> ss = mCapture.getSupportedPreviewSizes();
                            Size s = ss.get(ss.size() - 6);
                            mCapture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, s.width);
                            mCapture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, s.height);
                            sHandler.obtainMessage(EVENT_CAPTURE, MainActivity.this).sendToTarget();
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCaptureImageView.setImageBitmap(null);
        mWorkCaches.release();
        if (mCapture != null) {
            mCapture.release();
            mCapture = null;
        }
        sHandler.removeMessages(EVENT_CAPTURE);
    }

    private TagDetector mTagDetector;

    private class TagDetector {
        FeatureDetector detector;

        DescriptorExtractor extractor;

        DescriptorMatcher matcher;

        boolean resetMatch = false;

        int takeMatchNum = 0;

        List<TagItem> tagItems = new ArrayList<TagItem>();

        public TagDetector() {
            // detector = FeatureDetector.create(FeatureDetector.ORB);
            // extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
            // matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
            detector = FeatureDetector.create(FeatureDetector.BRISK);
            extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
            matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        }
    }

    private void updateCapture() {
        if (mCapture.grab()) {
            Mat m1 = mWorkCaches.getWorkMat(0);
            mCapture.retrieve(m1);
            Mat m2 = mWorkCaches.getWorkMat(1, m1.width(), m1.height(), m1.type());
            Mat m3 = mWorkCaches.getWorkMat(2, m1.width(), m1.height(), m1.type());
            Mat m4 = mWorkCaches.getWorkMat(3, m1.height(), m1.width(), m1.type());
            Bitmap bm = mWorkCaches.getWorkBitmap(0, m4.cols(), m4.rows());
            { // Convert and rotate from raw data
              // BGR→RGB
                Imgproc.cvtColor(m1, m2, Imgproc.COLOR_BGR2RGB);
                // Imgproc.cvtColor(m1, m2, Imgproc.COLOR_BGR2RGB);

                // rotate2deg
                ImageUtil.rotate90(m2, m3, m4);
            }

            { // Tag detection
                Rect rect = new Rect(m4.cols() / 4, m4.rows() / 4, m4.cols() / 2, m4.rows() / 2);

                KeyPoint[] keypoints;
                Mat descriptors = mWorkCaches.getWorkMat(4);
                { // Extract keypoints
                    MatOfKeyPoint mokp = new MatOfKeyPoint();
                    mTagDetector.detector.detect(m4, mokp);
                    mTagDetector.extractor.compute(m4, mokp, descriptors);
                    keypoints = mokp.toArray();
                }
                if (mTagDetector.resetMatch) {
                    mTagDetector.resetMatch = false;
                    mTagDetector.takeMatchNum = 5;
                    mTagDetector.tagItems.clear();
                }
                if (mTagDetector.takeMatchNum > 0) { // Extract keypoints for
                                                     // tag
                    mTagDetector.takeMatchNum--;
                    Mat tmp = m4.submat(rect);
                    MatOfKeyPoint mokp = new MatOfKeyPoint();
                    Mat queryDescriptors = new Mat();
                    mTagDetector.detector.detect(tmp, mokp);
                    mTagDetector.extractor.compute(tmp, mokp, queryDescriptors);
                    TagItem item = new TagItem(rect.width, rect.height, queryDescriptors,
                            mokp.toArray());
                    mTagDetector.tagItems.add(item);
                }
                { // Marking keypoints
                    for (KeyPoint kp : keypoints) {
                        Scalar color = new Scalar(0xFF, 0xFF, 0xFF, 0xFF);
                        Core.circle(m4, kp.pt, 10, color);
                    }
                }

                for (TagItem item : mTagDetector.tagItems) {
                    List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
                    Mat queryDescriptors = item.getQueryDescriptors();
                    Mat trainDescriptors = descriptors;
                    mTagDetector.matcher.knnMatch(queryDescriptors, trainDescriptors, matches, 2);

                    List<DMatch> good_matches = new ArrayList<DMatch>();
                    for (MatOfDMatch mm : matches) {
                        DMatch[] m = mm.toArray();
                        if (m.length >= 2 && (m[0].distance < 0.6 * m[1].distance)) {
                            good_matches.add(m[0]);
                        }
                    }
                    if (good_matches.size() >= 4) {
                        float maxDistance = 1;
                        { // calculate maxDistance
                            for (MatOfDMatch mm : matches) {
                                for (DMatch m : mm.toArray()) {
                                    if (maxDistance < m.distance) {
                                        maxDistance = m.distance;
                                    }
                                }
                            }
                        }

                        MatOfPoint2f srcPoints;
                        MatOfPoint2f dstPoints;
                        { // Pickup good matches to calculate homography
                            List<Point> src = new ArrayList<Point>();
                            List<Point> dst = new ArrayList<Point>();
                            for (DMatch m : good_matches) {
                                KeyPoint kp = keypoints[m.trainIdx];
                                Scalar color = new Scalar(0xFF * m.distance / maxDistance, 0xFF, 0,
                                        0);
                                Core.circle(m4, kp.pt, 10, color, -1);
                                src.add(item.getQueryKeyPoints()[m.queryIdx].pt);
                                dst.add(keypoints[m.trainIdx].pt);
                            }
                            srcPoints = new MatOfPoint2f(src.toArray(new Point[src.size()]));
                            dstPoints = new MatOfPoint2f(dst.toArray(new Point[dst.size()]));
                        }
                        Mat homography = Calib3d.findHomography(srcPoints, dstPoints,
                                Calib3d.RANSAC, 3);
                        // { // Create string of homography to output log
                        // StringBuilder sb = new StringBuilder();
                        // double[] f = new double[1];
                        // for (int r = 0; r < 3; r++) {
                        // for (int c = 0; c < 3; c++) {
                        // if (c > 0) {
                        // sb.append(" ,");
                        // }
                        // homography.get(r, c, f);
                        // sb.append(String.format("%.2f", f[0]));
                        // }
                        // sb.append("\n");
                        // }
                        // Log.d(Constants.TAG, sb.toString());
                        // }
                        Point[] srcPt = new Point[4];
                        Point[] dstPt = new Point[4];
                        for (int i = 0; i < srcPt.length; i++) {
                            srcPt[i] = new Point();
                        }
                        MatOfPoint2f src = new MatOfPoint2f( //
                                new Point(0, 0), //
                                new Point(rect.width, 0), //
                                new Point(rect.width, rect.height), //
                                new Point(0, rect.height));
                        MatOfPoint2f dst = new MatOfPoint2f(srcPt);
                        Core.perspectiveTransform(src, dst, homography);
                        dstPt = dst.toArray();
                        Scalar color = new Scalar(0xFF, 0xFF, 0, 0);
                        Core.line(m4, dstPt[0], dstPt[1], color);
                        Core.line(m4, dstPt[1], dstPt[2], color);
                        Core.line(m4, dstPt[2], dstPt[3], color);
                        Core.line(m4, dstPt[3], dstPt[0], color);
                    }
                }
            }
            Utils.matToBitmap(m4, bm);
            mCaptureImageView.setImageBitmap(bm);
        }
    }
}
