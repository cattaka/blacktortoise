
package net.blacktortoise.android.ai;

import java.util.ArrayList;
import java.util.List;

import net.blacktortoise.android.ai.core.MyPreferences;
import net.blacktortoise.android.ai.tagdetector.TagDetectResult;
import net.blacktortoise.android.ai.tagdetector.TagItem;
import net.blacktortoise.android.ai.util.MyCapture;
import net.blacktortoise.android.ai.util.WorkCaches;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.VideoCapture;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

public class TagTrackerActivity extends Activity {
    private static final int EVENT_CAPTURE = 1;

    private WorkCaches mWorkCaches;

    private MyCapture mMyCapture;

    private ImageView mCaptureImageView;

    private int mSeqCapMat;

    private int mSeqResultMat;

    private static Handler sHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            TagTrackerActivity target = (TagTrackerActivity)msg.obj;
            if (msg.what == EVENT_CAPTURE) {
                target.updateCapture();
                obtainMessage(EVENT_CAPTURE, target).sendToTarget();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_tracker);

        mWorkCaches = new WorkCaches();
        mSeqCapMat = mWorkCaches.getNextWorkCachesSeq();
        mSeqResultMat = mWorkCaches.getNextWorkCachesSeq();

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

        MyPreferences pref = new MyPreferences(this);
        {
            mTagDetector = new TagDetector();
            VideoCapture capture = new VideoCapture();
            mMyCapture = new MyCapture(mWorkCaches, capture);
            mMyCapture.open(pref.isRotateCamera(), pref.isReverseCamera(),
                    pref.getPreviewSizeAsSize());
            sHandler.obtainMessage(EVENT_CAPTURE, TagTrackerActivity.this).sendToTarget();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCaptureImageView.setImageBitmap(null);
        mWorkCaches.release();
        if (mMyCapture != null) {
            mMyCapture.release();
            mMyCapture = null;
        }
        sHandler.removeMessages(EVENT_CAPTURE);
    }

    private TagDetector mTagDetector;

    private class TagDetector extends net.blacktortoise.android.ai.tagdetector.TagDetector {
        boolean resetMatch = false;

        int takeMatchNum = 0;
    }

    private void updateCapture() {
        Mat cap = mWorkCaches.getWorkMat(mSeqCapMat);
        if (mMyCapture.takePicture(cap)) {

            Rect rect = new Rect((int)(cap.width() / 4f), (int)(cap.height() / 4f),
                    cap.width() / 2, cap.height() / 2);
            if (mTagDetector.resetMatch) {
                mTagDetector.resetMatch = false;
                mTagDetector.takeMatchNum = 5;
                mTagDetector.removeTagItem(0);
                TagItem tagItem = new TagItem(rect.width, rect.height);
                mTagDetector.putTagItem(0, tagItem);
            }
            if (mTagDetector.takeMatchNum > 0) { // Extract keypoints for
                                                 // tag
                mTagDetector.takeMatchNum--;
                Mat tmp = cap.submat(rect);
                mTagDetector.upgradeTagItem(mTagDetector.getTagItem(0), tmp);
            }

            // ======================

            { // Tag detection
                Mat resultMat = mWorkCaches.getWorkMat(mSeqResultMat, cap.height(), cap.width(),
                        cap.type());
                cap.copyTo(resultMat);
                Bitmap bm = mWorkCaches.getWorkBitmap(0, cap.cols(), cap.rows());
                Utils.matToBitmap(cap, bm);
                List<TagDetectResult> results = new ArrayList<TagDetectResult>();
                mTagDetector.detectTags(results, cap, resultMat);
                Utils.matToBitmap(resultMat, bm);
                mCaptureImageView.setImageBitmap(bm);
            }
        }
    }
}
