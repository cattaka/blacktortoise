
package net.cattaka.android.ultimatetank.camera;

import net.cattaka.android.ultimatetank.fragment.BaseFragment.IBaseFragmentAdapter;

public interface ICameraManager {
    public void onResume();

    public void onPause();

    public boolean isEnablePreview();

    public void setEnablePreview(boolean enablePreview);

    public void setup(ICameraManagerAdapter cameraManagerAdapter,
            IBaseFragmentAdapter baseFragmentAdapter);
}
