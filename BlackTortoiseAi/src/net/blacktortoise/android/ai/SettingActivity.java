
package net.blacktortoise.android.ai;

import java.util.ArrayList;
import java.util.List;

import net.blacktortoise.android.ai.core.MyPreferences;
import net.blacktortoise.android.ai.core.TagDetectorAlgorism;

import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;

import android.app.Activity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

public class SettingActivity extends Activity {
    private Switch mRotateCameraView;

    private Switch mReverseCameraView;

    private Spinner mPreviewSizeView;

    private Spinner mTagDetectorAlgorismView;

    private MyPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mRotateCameraView = (Switch)findViewById(R.id.rotateCameraSwitch);
        mReverseCameraView = (Switch)findViewById(R.id.reverseCameraSwitch);
        mPreviewSizeView = (Spinner)findViewById(R.id.previewSizeSpinner);
        mTagDetectorAlgorismView = (Spinner)findViewById(R.id.tagDetectorAlgorismSpinner);
    }

    @Override
    protected void onResume() {
        super.onResume();

        {
            List<String> sizeStrs = new ArrayList<String>();
            {
                VideoCapture mCapture = new VideoCapture();
                mCapture.open(0);
                List<Size> sizes = mCapture.getSupportedPreviewSizes();
                mCapture.release();
                for (Size size : sizes) {
                    sizeStrs.add((int)size.width + "x" + (int)size.height);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, sizeStrs);
            mPreviewSizeView.setAdapter(adapter);
        }
        {
            ArrayAdapter<TagDetectorAlgorism> adapter = new ArrayAdapter<TagDetectorAlgorism>(this,
                    android.R.layout.simple_list_item_1, TagDetectorAlgorism.values());
            mTagDetectorAlgorismView.setAdapter(adapter);
        }

        {
            mPreferences = new MyPreferences(this);
            mRotateCameraView.setChecked(mPreferences.isRotateCamera());
            mReverseCameraView.setChecked(mPreferences.isReverseCamera());
            setSelection(mPreviewSizeView, mPreferences.getPreviewSize());
            setSelection(mTagDetectorAlgorismView, mPreferences.getTagDetectorAlgorism());
        }
    }

    public void setSelection(AdapterView<?> view, Object obj) {
        if (obj != null) {
            for (int i = 0; i < view.getCount(); i++) {
                if (obj.equals(view.getItemAtPosition(i))) {
                    view.setSelection(i);
                    break;
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreferences.edit();
        mPreferences.putRotateCamera(mRotateCameraView.isChecked());
        mPreferences.putReverseCamera(mReverseCameraView.isChecked());
        mPreferences.putPreviewSize(String.valueOf(mPreviewSizeView.getSelectedItem()));
        mPreferences.putTagDetectorAlgorism((TagDetectorAlgorism)mTagDetectorAlgorismView
                .getSelectedItem());
        mPreferences.commit();
        mPreferences = null;
    }
}
