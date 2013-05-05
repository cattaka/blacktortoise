
package net.cattaka.android.ultimatetank.camera;

import net.cattaka.android.ultimatetank.fragment.BaseFragment.IBaseFragmentAdapter;
import net.cattaka.android.ultimatetank.usb.ICommandAdapter;
import net.cattaka.android.ultimatetank.usb.data.MyPacket;
import net.cattaka.android.ultimatetank.usb.data.OpCode;
import net.cattaka.libgeppa.data.ConnectionCode;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.thread.IConnectionThreadListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class RemoteCameraManager implements ICameraManager, IConnectionThreadListener<MyPacket> {
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
        mBaseFragmentAdapter.registerConnectionThreadListener(this);
        setEnablePreview(mEnablePreview);
    }

    @Override
    public void onPause() {
        mBaseFragmentAdapter.unregisterConnectionThreadListener(this);
    }

    @Override
    public boolean isEnablePreview() {
        return mEnablePreview;
    }

    @Override
    public void setEnablePreview(boolean enablePreview) {
        mEnablePreview = enablePreview;
        if (mEnablePreview && !mRequested) {
            ICommandAdapter commandAdapter = mBaseFragmentAdapter.getCommandAdapter();
            if (commandAdapter != null) {
                mRequested = commandAdapter.sendRequestCameraImage();
            }
        }
    }

    @Override
    public void onConnectionStateChanged(ConnectionState state, ConnectionCode code) {
        // none
    }

    @Override
    public void onReceive(MyPacket packet) {
        if (packet.getOpCode() == OpCode.CAMERA_IMAGE) {
            if (mEnablePreview) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(packet.getData(), 0,
                        packet.getDataLen());
                if (bitmap != null) {
                    mCameraManagerAdapter.onPictureTaken(bitmap, this);
                    ICommandAdapter commandAdapter = mBaseFragmentAdapter.getCommandAdapter();
                    if (commandAdapter != null) {
                        mRequested = commandAdapter.sendRequestCameraImage();
                    }
                }
            }
        }
    }
}
