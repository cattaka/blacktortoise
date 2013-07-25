
package net.blacktortoise.android.ai;

import java.util.List;

import net.blacktortoise.android.ai.action.ConsoleDto;
import net.blacktortoise.android.ai.tagdetector.TagDetector;
import net.blacktortoise.android.ai.thread.ActionThread;
import net.blacktortoise.android.ai.thread.ActionUtil;
import net.blacktortoise.android.ai.thread.ActionUtil.IActionUtilListener;
import net.blacktortoise.android.ai.util.BlackTortoiseServiceWrapperEx;
import net.blacktortoise.android.ai.util.IndicatorDrawer;
import net.blacktortoise.android.ai.util.MyCapture;
import net.blacktortoise.android.ai.util.WorkCaches;
import net.blacktortoise.androidlib.BlackTortoiseFunctions;
import net.blacktortoise.androidlib.IBlackTortoiseService;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageView;

public class DebugActivity extends Activity {
    private WorkCaches mWorkCaches;

    private ImageView mCaptureImageView;

    private ImageView mMoveIndicator;

    private ImageView mHeadIndicator;

    private TagDetector mTagDetector;

    private ActionUtil mActionUtil;

    private BlackTortoiseServiceWrapperEx mServiceWrapper;

    private MyCapture mMyCapture;

    private ActionThread mActionThread;

    private IActionUtilListener mActionUtilListener = new IActionUtilListener() {
        private IndicatorDrawer mIndicatorDrawer = new IndicatorDrawer();

        private Bitmap mMoveBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_4444);

        private Bitmap mHeadBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_4444);

        @Override
        public void onUpdateConsole(ConsoleDto dto) {
            mIndicatorDrawer.drawMove(mMoveBitmap, dto);
            mIndicatorDrawer.drawMove(mHeadBitmap, dto);
            mMoveIndicator.setImageBitmap(mMoveBitmap);
            mHeadIndicator.setImageBitmap(mHeadBitmap);
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceWrapper = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IBlackTortoiseService btService = IBlackTortoiseService.Stub.asInterface(service);
            mServiceWrapper = new BlackTortoiseServiceWrapperEx(btService);
            prepareActionThread();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        mWorkCaches = new WorkCaches();
        mCaptureImageView = (ImageView)findViewById(R.id.captureImageView);
        mMoveIndicator = (ImageView)findViewById(R.id.moveIndicator);
        mHeadIndicator = (ImageView)findViewById(R.id.headIndicator);
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
                            VideoCapture capture = new VideoCapture();
                            capture.open(0);
                            List<Size> ss = capture.getSupportedPreviewSizes();
                            Size s = ss.get(ss.size() - 6);
                            capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, s.width);
                            capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, s.height);
                            mMyCapture = new MyCapture(mWorkCaches, capture);
                            prepareActionThread();
                        }
                    }
                });
        Intent service = BlackTortoiseFunctions.createServiceIntent();
        bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        {
            mActionThread.stopSafety();
            mActionThread = null;
        }

        if (mActionUtil != null) {
            mActionUtil = null;
        }

        if (mServiceWrapper != null) {
            unbindService(mServiceConnection);
            ;
            mServiceWrapper = null;
        }

        if (mTagDetector != null) {
            // TODO リークしてないか？
            mTagDetector = null;
        }

        if (mMyCapture != null) {
            mMyCapture.release();
            mMyCapture = null;
        }

        mWorkCaches.release();
    }

    private void prepareActionThread() {
        if (mActionUtil == null) {
            if (mMyCapture != null && mServiceWrapper != null) {
                mActionUtil = new ActionUtil(mWorkCaches, mMyCapture, mTagDetector,
                        mServiceWrapper, mActionUtilListener);
                mActionThread = new ActionThread(mActionUtil);
                mActionThread.start();
            }
        }

    }
}