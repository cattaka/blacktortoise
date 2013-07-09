
package net.blacktortoise.android.ai.util;

import net.blacktortoise.android.ai.action.IActionUtil;

import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class MyCapture {
    private static int sM1Seq = -1;

    private static int sM2Seq = -1;

    private static int sM3Seq = -1;

    private static int sM4Seq = -1;

    private VideoCapture mVideoCapture;

    private IActionUtil mUtil;

    public MyCapture(IActionUtil util, VideoCapture videoCapture) {
        super();
        mVideoCapture = videoCapture;
        setup(util);
    }

    private void setup(IActionUtil util) {
        if (sM1Seq < 0) {
            sM1Seq = util.getNextWorkCachesSeq();
        }
        if (sM2Seq < 0) {
            sM2Seq = util.getNextWorkCachesSeq();
        }
        if (sM3Seq < 0) {
            sM3Seq = util.getNextWorkCachesSeq();
        }
        if (sM4Seq < 0) {
            sM4Seq = util.getNextWorkCachesSeq();
        }
    }

    public boolean takePicture(Mat dst) {
        WorkCaches workCaches = mUtil.getWorkCaches();
        Mat m1 = workCaches.getWorkMat(sM1Seq);
        if (mVideoCapture.grab()) {
            mVideoCapture.retrieve(m1);
            Mat m2 = workCaches.getWorkMat(sM2Seq, m1.width(), m1.height(), m1.type());
            Mat m3 = workCaches.getWorkMat(sM3Seq, m1.width(), m1.height(), m1.type());
            { // Convert and rotate from raw data
              // BGR→RGB
                Imgproc.cvtColor(m1, m2, Imgproc.COLOR_BGR2RGB);
                // Imgproc.cvtColor(m1, m2, Imgproc.COLOR_BGR2RGB);

                // rotate2deg
                ImageUtil.rotate90(m2, m3, dst);
            }
            return true;
        } else {
            return false;
        }

    }
}
