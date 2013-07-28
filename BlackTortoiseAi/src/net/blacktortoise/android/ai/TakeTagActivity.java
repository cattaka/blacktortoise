
package net.blacktortoise.android.ai;

import java.util.ArrayList;
import java.util.List;

import net.blacktortoise.android.ai.core.MyPreferences;
import net.blacktortoise.android.ai.db.DbHelper;
import net.blacktortoise.android.ai.model.TagItemModel;
import net.blacktortoise.android.ai.tagdetector.TagDetectResult;
import net.blacktortoise.android.ai.tagdetector.TagDetector;
import net.blacktortoise.android.ai.tagdetector.TagItem;
import net.blacktortoise.android.ai.util.MyCapture;
import net.blacktortoise.android.ai.util.WorkCaches;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.VideoCapture;

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

    private MyCapture mMyCapture;

    private ImageView mCaptureImageView;

    private int mSeqCapMat;

    private int mSeqResultMat;

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
        mSeqCapMat = mWorkCaches.getNextWorkCachesSeq();
        mSeqResultMat = mWorkCaches.getNextWorkCachesSeq();

        mCaptureImageView = (ImageView)findViewById(R.id.captureImageView);
        mCaptureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTagTaker.resetMatch = true;
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

        MyPreferences pref = new MyPreferences(this);
        {
            mTagDetector = pref.getTagDetectorAlgorism().createTagDetector();
            VideoCapture capture = new VideoCapture();
            mMyCapture = new MyCapture(mWorkCaches, capture);
            mMyCapture.open(pref.isRotateCamera(), pref.isReverseCamera(),
                    pref.getPreviewSizeAsSize());
            sHandler.obtainMessage(EVENT_CAPTURE, TakeTagActivity.this).sendToTarget();
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

    private TagTaker mTagTaker = new TagTaker();

    private class TagTaker {
        boolean resetMatch = false;

        int takeMatchNum = 0;
    }

    private void updateCapture() {
        Mat m4 = mWorkCaches.getWorkMat(mSeqCapMat);
        if (mMyCapture.takePicture(m4)) {
            Rect rect = new Rect((int)(m4.width() / 4f), (int)(m4.height() / 4f), m4.width() / 2,
                    m4.height() / 2);
            if (mTagTaker.resetMatch) {
                mTagTaker.resetMatch = false;
                mTagTaker.takeMatchNum = 5;
                mTagDetector.removeTagItem(0);
                TagItem tagItem = new TagItem(rect.width, rect.height);
                mTagDetector.putTagItem(0, tagItem);
                mBitmaps.clear();
            }
            if (mTagTaker.takeMatchNum > 0) { // Extract keypoints for
                                              // tag
                mTagTaker.takeMatchNum--;
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
                Mat resultMat = mWorkCaches.getWorkMat(5, m4.height(), m4.width(), m4.type());
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
