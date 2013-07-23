
package net.blacktortoise.android.ai.util;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class MyCapture {
    private static int sM1Seq = -1;

    private static int sM2Seq = -1;

    private static int sM3Seq = -1;

    private static int sM4Seq = -1;

    private VideoCapture mVideoCapture;

    private WorkCaches mWorkCaches;

    private boolean mEnableInvert = false;

    public MyCapture(WorkCaches workCaches, VideoCapture videoCapture) {
        super();
        mWorkCaches = workCaches;
        mVideoCapture = videoCapture;
        setup(workCaches);
    }

    private void setup(WorkCaches workCaches) {
        if (sM1Seq < 0) {
            sM1Seq = workCaches.getNextWorkCachesSeq();
        }
        if (sM2Seq < 0) {
            sM2Seq = workCaches.getNextWorkCachesSeq();
        }
        if (sM3Seq < 0) {
            sM3Seq = workCaches.getNextWorkCachesSeq();
        }
        if (sM4Seq < 0) {
            sM4Seq = workCaches.getNextWorkCachesSeq();
        }
    }

    public boolean takePicture(Mat dst) {
        Mat m1 = mWorkCaches.getWorkMat(sM1Seq);
        if (mVideoCapture.grab()) {
            mVideoCapture.retrieve(m1);
            Mat m2 = mWorkCaches.getWorkMat(sM2Seq, m1.width(), m1.height(), m1.type());
            Mat m3 = mWorkCaches.getWorkMat(sM3Seq, m1.width(), m1.height(), m1.type());
            // Mat m4 = workCaches.getWorkMat(sM3Seq, m1.width(), m1.height(),
            // m1.type());
            { // Convert and rotate from raw data
              // BGR→RGB
                Imgproc.cvtColor(m1, m2, Imgproc.COLOR_BGR2RGB);
                // Imgproc.cvtColor(m1, m2, Imgproc.COLOR_BGR2RGB);

                if (mEnableInvert) {
                    // rotate2deg
                    Core.flip(m2, m3, 0);
                    Core.transpose(m3, dst);
                } else {
                    // rotate2deg
                    // Core.flip(m2, m3, 0);
                    Core.transpose(m2, dst);
                }
            }
            return true;
        } else {
            return false;
        }

    }

    public int getWidth() {
        return (int)mVideoCapture.get(Highgui.CV_CAP_PROP_FRAME_WIDTH);
    }

    public int getHeight() {
        return (int)mVideoCapture.get(Highgui.CV_CAP_PROP_FRAME_HEIGHT);
    }

    public void release() {
        mVideoCapture.release();
    }

}
