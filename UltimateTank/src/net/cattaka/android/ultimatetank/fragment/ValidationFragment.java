
package net.cattaka.android.ultimatetank.fragment;

import java.util.Locale;

import net.cattaka.android.ultimatetank.NormalizedOnTouchListener;
import net.cattaka.android.ultimatetank.R;
import net.cattaka.android.ultimatetank.net.data.MyPacket;
import net.cattaka.android.ultimatetank.net.data.OpCode;
import net.cattaka.android.ultimatetank.usb.ICommandAdapter;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class ValidationFragment extends BaseFragment implements OnClickListener {
    private OnTouchListener mOnTouchListener = new NormalizedOnTouchListener() {
        long lastSendHeadTime;

        long lastSendMoveTime;

        @Override
        public boolean onTouch(View v, MotionEvent event, float rx, float ry) {
            if (v.getId() == R.id.controller_head) {
                long t = SystemClock.elapsedRealtime();
                if (t - lastSendHeadTime > 100 || event.getActionMasked() == MotionEvent.ACTION_UP) {
                    { // Displayes values on TextView
                        String text = String.format(Locale.getDefault(), "(%.2f,%.2f)", rx, ry);
                        mHeadValueText.setText(text);
                    }
                    { // Sends command
                        ICommandAdapter adapter = getCommandAdapter();
                        if (adapter != null) {
                            adapter.sendHead(rx, ry);
                        }
                    }
                    lastSendHeadTime = t;
                }
            } else if (v.getId() == R.id.controller_move) {
                long t = SystemClock.elapsedRealtime();
                if (t - lastSendMoveTime > 100 || event.getActionMasked() == MotionEvent.ACTION_UP) {
                    { // Displayes values on TextView
                        String text = String.format(Locale.getDefault(), "(%.2f,%.2f)", rx, ry);
                        mMoveValueText.setText(text);
                    }
                    { // Sends command
                        ICommandAdapter adapter = getCommandAdapter();
                        if (adapter != null) {
                            adapter.sendMove(ry, rx);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_validation, null);

        // Pickup views
        mHeadValueText = (TextView)view.findViewById(R.id.head_value_text);
        mMoveValueText = (TextView)view.findViewById(R.id.move_value_text);

        // Binds event listeners
        view.findViewById(R.id.controller_head).setOnTouchListener(mOnTouchListener);
        view.findViewById(R.id.controller_move).setOnTouchListener(mOnTouchListener);
        view.findViewById(R.id.sendButton).setOnClickListener(this);
        view.findViewById(R.id.clearButton).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sendButton) {
            ICommandAdapter adapter = getCommandAdapter();
            if (adapter != null) {
                EditText sendText = (EditText)getView().findViewById(R.id.sendEdit);
                byte[] data = String.valueOf(sendText.getText()).getBytes();
                MyPacket packet = new MyPacket(OpCode.ECHO, data.length, data);
                adapter.sendPacket(packet);
            }
        } else if (v.getId() == R.id.clearButton) {
            TextView receivedText = (TextView)getView().findViewById(R.id.receivedText);
            receivedText.setText("");
        }

    }

    @Override
    public void onReceive(MyPacket packet) {
        super.onReceive(packet);
        if (packet.getOpCode() == OpCode.ECHO) {
            byte[] data = new byte[packet.getDataLen()];
            System.arraycopy(packet.getData(), 0, data, 0, data.length);
            String str = new String(data);
            TextView receivedText = (TextView)getView().findViewById(R.id.receivedText);
            receivedText.setText(receivedText.getText() + str);
        }
    }
}
