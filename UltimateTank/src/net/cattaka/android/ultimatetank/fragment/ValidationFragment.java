
package net.cattaka.android.ultimatetank.fragment;

import java.util.Locale;

import net.cattaka.android.ultimatetank.NormalizedOnTouchListener;
import net.cattaka.android.ultimatetank.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ValidationFragment extends BaseFragment {
    private OnTouchListener mOnTouchListener = new NormalizedOnTouchListener(255) {
        @Override
        public boolean onTouch(View v, MotionEvent event, int rx, int ry) {
            if (v.getId() == R.id.controller_head) {
                String text = String.format(Locale.getDefault(), "(%3d,%3d)", rx, ry);
                mHeadValueText.setText(text);
            } else if (v.getId() == R.id.controller_move) {
                String text = String.format(Locale.getDefault(), "(%3d,%3d)", rx, ry);
                mMoveValueText.setText(text);
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

        return view;
    }

}
