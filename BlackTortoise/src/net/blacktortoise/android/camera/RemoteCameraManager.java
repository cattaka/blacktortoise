
package net.blacktortoise.android.camera;

import net.blacktortoise.android.fragment.BaseFragment.IBaseFragmentAdapter;
import net.blacktortoise.androidlib.BlackTortoiseServiceWrapper;
import net.blacktortoise.androidlib.IDeviceAdapterListener;
import net.blacktortoise.androidlib.data.BtPacket;
import net.blacktortoise.androidlib.data.DeviceEventCode;
import net.blacktortoise.androidlib.data.DeviceInfo;
import net.blacktortoise.androidlib.data.DeviceState;
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
            BlackTortoiseServiceWrapper wrapper = mBaseFragmentAdapter.getServiceWrapper();
            if (wrapper != null) {
                mRequested = wrapper.requestCameraImage();
            }
        }
    }

    @Override
    public void onDeviceStateChanged(DeviceState state, DeviceEventCode code, DeviceInfo deviceInfo) {
        // none
    }

    @Override
    public void onReceive(BtPacket packet) {
        // none
    }

    @Override
    public void onReceiveCameraImage(int cameraIdx, Bitmap bitmap) {
        mCameraManagerAdapter.onPictureTaken(bitmap, this);
        BlackTortoiseServiceWrapper wrapper = mBaseFragmentAdapter.getServiceWrapper();
        if (wrapper != null) {
            mRequested = wrapper.requestCameraImage();
        }
    }
}
