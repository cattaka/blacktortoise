
package net.blacktortoise.android.ai;

import java.util.ArrayList;
import java.util.List;

import net.blacktortoise.android.ai.db.DbHelper;
import net.blacktortoise.android.ai.model.TagItemModel;
import net.blacktortoise.android.ai.tagdetector.TagDetectResult;
import net.blacktortoise.android.ai.tagdetector.TagItem;
import net.blacktortoise.android.ai.util.ImageUtil;
import net.blacktortoise.android.ai.util.WorkCaches;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class TakeTagActivity extends Activity implements OnClickListener {
    private static final int EVENT_CAPTURE = 1;

    private WorkCaches mWorkCaches;

    private VideoCapture mCapture;

    private ImageView mCaptureImageView;

    private List<Bitmap> mBitmaps;

    private static Handler sHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            TakeTagActivity target = (TakeTagActivity)msg.obj;
            if (msg.what == EVENT_CAPTURE) {
                target.updateCapture();
                obtainMessage(EVENT_CAPTURE, target).sendToTarget();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_tag);

        mBitmaps = new ArrayList<Bitmap>();
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

        findViewById(R.id.saveButton).setOnClickListener(this);
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

        {
            mTagDetector = new TagDetector();
            mCapture = new VideoCapture();
            mCapture.open(0);
            List<Size> ss = mCapture.getSupportedPreviewSizes();
            Size s = ss.get(ss.size() - 6);
            mCapture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, s.width);
            mCapture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, s.height);
            sHandler.obtainMessage(EVENT_CAPTURE, TakeTagActivity.this).sendToTarget();
        }
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

    private class TagDetector extends net.blacktortoise.android.ai.tagdetector.TagDetector {
        boolean resetMatch = false;

        int takeMatchNum = 0;
    }

    private void updateCapture() {
        if (mCapture.grab()) {
            Mat m1 = mWorkCaches.getWorkMat(0);
            mCapture.retrieve(m1);
            Mat m2 = mWorkCaches.getWorkMat(1, m1.width(), m1.height(), m1.type());
            Mat m3 = mWorkCaches.getWorkMat(2, m1.width(), m1.height(), m1.type());
            Mat m4 = mWorkCaches.getWorkMat(3, m1.height(), m1.width(), m1.type());
            { // Convert and rotate from raw data
              // BGR→RGB
                Imgproc.cvtColor(m1, m2, Imgproc.COLOR_BGR2RGB);
                // Imgproc.cvtColor(m1, m2, Imgproc.COLOR_BGR2RGB);

                // rotate2deg
                ImageUtil.rotate90(m2, m3, m4);
            }
            Rect rect = new Rect((int)(m4.width() / 4f), (int)(m4.height() / 4f), m4.width() / 2,
                    m4.height() / 2);
            if (mTagDetector.resetMatch) {
                mTagDetector.resetMatch = false;
                mTagDetector.takeMatchNum = 5;
                mTagDetector.removeTagItem(0);
                TagItem tagItem = new TagItem(rect.width, rect.height);
                mTagDetector.putTagItem(0, tagItem);
                mBitmaps.clear();
            }
            if (mTagDetector.takeMatchNum > 0) { // Extract keypoints for
                                                 // tag
                mTagDetector.takeMatchNum--;
                Mat tmp = m4.submat(rect);
                mTagDetector.upgradeTagItem(mTagDetector.getTagItem(0), tmp);
                {
                    Bitmap bitmap = Bitmap
                            .createBitmap(tmp.width(), tmp.height(), Config.ARGB_8888);
                    Utils.matToBitmap(tmp, bitmap);
                    mBitmaps.add(bitmap);
                }
            }

            // ======================

            { // Tag detection
                Mat resultMat = mWorkCaches.getWorkMat(5, m1.height(), m1.width(), m1.type());
                m4.copyTo(resultMat);
                Bitmap bm = mWorkCaches.getWorkBitmap(0, m4.cols(), m4.rows());
                Utils.matToBitmap(m4, bm);
                List<TagDetectResult> results = new ArrayList<TagDetectResult>();
                mTagDetector.detectTags(results, m4, resultMat);
                Utils.matToBitmap(resultMat, bm);
                mCaptureImageView.setImageBitmap(bm);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveButton) {
            if (mBitmaps.size() > 0) {
                TagItemModel model = new TagItemModel();
                model.setBitmaps(new ArrayList<Bitmap>(mBitmaps));
                model.updateThumbnail();

                DbHelper dbHelper = new DbHelper(this);
                try {
                    dbHelper.registerTagItemModel(model);
                } finally {
                    dbHelper.close();
                }
                finish();
            }
        }
    }
}
