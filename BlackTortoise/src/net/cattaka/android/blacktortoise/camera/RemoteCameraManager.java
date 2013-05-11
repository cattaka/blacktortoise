
package net.cattaka.android.blacktortoise.camera;

import net.cattaka.android.blacktortoise.common.IDeviceAdapterListener;
import net.cattaka.android.blacktortoise.common.IDeviceCommandAdapter;
import net.cattaka.android.blacktortoise.common.data.DeviceEventCode;
import net.cattaka.android.blacktortoise.common.data.DeviceState;
import net.cattaka.android.blacktortoise.fragment.BaseFragment.IBaseFragmentAdapter;
import android.graphics.Bitmap;

public class RemoteCameraManager implements ICameraManager, IDeviceAdapterListener {
    private ICameraManagerAdapter mCameraManagerAdapter;

    private IBaseFragmentAdapter mBaseFragmentAdapter;

    private boolean mEnablePreview;

    private boolean mRequested;

    public RemoteCameraManager() {
    }

    @Override
    public void setup(ICameraManagerAdapter cameraManagerAdapter,
            IBaseFragmentAdapter baseFragmentAdapter) {
        mCameraManagerAdapter = cameraManagerAdapter;
        mBaseFragmentAdapter = baseFragmentAdapter;
    }

    @Override
    public void onResume() {
        mBaseFragmentAdapter.registerDeviceAdapterListener(this);
        setEnablePreview(mEnablePreview);
    }

    @Override
    public void onPause() {
        mBaseFragmentAdapter.unregisterDeviceAdapterListener(this);
    }

    @Override
    public boolean isEnablePreview() {
        return mEnablePreview;
    }

    @Override
    public void setEnablePreview(boolean enablePreview) {
        mEnablePreview = enablePreview;
        if (mEnablePreview && !mRequested) {
            IDeviceCommandAdapter commandAdapter = mBaseFragmentAdapter.getCommandAdapter();
            if (commandAdapter != null) {
                mRequested = commandAdapter.sendRequestCameraImage();
            }
        }
    }

    @Override
    public void onDeviceStateChanged(DeviceState state, DeviceEventCode code) {
        // none
    }

    @Override
    public void onReceiveEcho(byte[] data) {
        // none
    }

    @Override
    public void onReceiveCameraImage(int cameraIdx, Bitmap bitmap) {
        mCameraManagerAdapter.onPictureTaken(bitmap, this);
        IDeviceCommandAdapter commandAdapter = mBaseFragmentAdapter.getCommandAdapter();
        if (commandAdapter != null) {
            mRequested = commandAdapter.sendRequestCameraImage();
        }
    }
}
