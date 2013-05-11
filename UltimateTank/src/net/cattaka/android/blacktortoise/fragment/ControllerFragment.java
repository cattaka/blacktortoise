
package net.cattaka.android.blacktortoise.fragment;

import java.util.Locale;

import net.cattaka.android.blacktortoise.R;
import net.cattaka.android.blacktortoise.camera.ICameraManager;
import net.cattaka.android.blacktortoise.camera.ICameraManagerAdapter;
import net.cattaka.android.blacktortoise.common.IDeviceCommandAdapter;
import net.cattaka.android.blacktortoise.common.data.BtPacket;
import net.cattaka.android.blacktortoise.common.data.OpCode;
import net.cattaka.android.blacktortoise.util.CommandAdapterUtil;
import net.cattaka.android.blacktortoise.util.NormalizedOnTouchListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ControllerFragment extends BaseFragment implements OnClickListener {
    private OnTouchListener mOnTouchListener = new NormalizedOnTouchListener() {
        long lastSendHeadTime;

        long lastSendMoveTime;

        @Override
        public boolean onTouch(View v, MotionEvent event, float rx, float ry) {
            if (v.getId() == R.id.controller_head) {
                long t = SystemClock.elapsedRealtime();
                if (t - lastSendHeadTime > 100 || event.getActionMasked() == MotionEvent.ACTION_UP) {
                    float yaw = (1 - rx);
                    float pitch = (1 - ry);
                    ;
                    { // Displayes values on TextView
                        String text = String.format(Locale.getDefault(), "(yaw,pitch)=(%.2f,%.2f)",
                                yaw, pitch);
                        mHeadValueText.setText(text);
                    }
                    { // Sends command
                        IDeviceCommandAdapter adapter = getCommandAdapter();
                        if (adapter != null) {
                            adapter.sendHead(yaw, pitch);
                        }
                    }
                    lastSendHeadTime = t;
                }
            } else if (v.getId() == R.id.controller_move) {
                long t = SystemClock.elapsedRealtime();
                if (t - lastSendMoveTime > 100 || event.getActionMasked() == MotionEvent.ACTION_UP) {
                    float forward = -(ry * 2 - 1);
                    float turn = rx * 2 - 1;
                    { // Displayes values on TextView
                        String text = String.format(Locale.getDefault(),
                                "(forward,turn)=(%.2f,%.2f)", forward, turn);
                        mMoveValueText.setText(text);
                    }
                    { // Sends command
                        IDeviceCommandAdapter adapter = getCommandAdapter();
                        if (adapter != null) {
                            CommandAdapterUtil.sendMove(adapter, forward, turn);
                        }
                    }
                    lastSendMoveTime = t;
                }
            }
            return true;
        }
    };

    private TextView mHeadValueText;

    private TextView mMoveValueText;

    private SurfaceView mCameraSurfaceView;

    private ImageView mCameraImageView;

    private ICameraManager mCameraManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controller, null);

        // Pickup views
        mHeadValueText = (TextView)view.findViewById(R.id.head_value_text);
        mMoveValueText = (TextView)view.findViewById(R.id.move_value_text);
        mCameraSurfaceView = (SurfaceView)view.findViewById(R.id.cameraSurfaceView);
        mCameraImageView = (ImageView)view.findViewById(R.id.cameraImageView);

        // Binds event listeners
        view.findViewById(R.id.controller_head).setOnTouchListener(mOnTouchListener);
        view.findViewById(R.id.controller_move).setOnTouchListener(mOnTouchListener);
        view.findViewById(R.id.sendButton).setOnClickListener(this);
        view.findViewById(R.id.clearButton).setOnClickListener(this);
        mCameraImageView.setOnClickListener(this);

        mCameraManager = createCameraManager(new ICameraManagerAdapter() {
            @Override
            public SurfaceView getSurfaceView() {
                return mCameraSurfaceView;
            }

            @Override
            public void onPictureTaken(Bitmap bitmap, ICameraManager cameraManager) {
                mCameraImageView.setImageBitmap(bitmap);
            }
        });
        mCameraManager.setEnablePreview(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraManager.onPause();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sendButton) {
            IDeviceCommandAdapter adapter = getCommandAdapter();
            if (adapter != null) {
                EditText sendText = (EditText)getView().findViewById(R.id.sendEdit);
                byte[] data = String.valueOf(sendText.getText()).getBytes();
                BtPacket packet = new BtPacket(OpCode.ECHO, data.length, data);
                adapter.sendPacket(packet);
            }
        } else if (v.getId() == R.id.clearButton) {
            TextView receivedText = (TextView)getView().findViewById(R.id.receivedText);
            receivedText.setText("");
        } else if (v.getId() == R.id.cameraImageView) {
            mCameraManager.setEnablePreview(!mCameraManager.isEnablePreview());
        }

    }

    @Override
    public void onReceiveEcho(byte[] data) {
        super.onReceiveEcho(data);
        String str = new String(data);
        TextView receivedText = (TextView)getView().findViewById(R.id.receivedText);
        receivedText.setText(receivedText.getText() + str);
    }
}
