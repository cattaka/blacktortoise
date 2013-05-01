
package net.cattaka.android.ultimatetank.fragment;

import net.cattaka.android.ultimatetank.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class MainMenuFragment extends BaseFragment implements OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, null);

        // Bind event listeners
        view.findViewById(R.id.validationButton).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.validationButton) {
            ValidationFragment nextFragment = new ValidationFragment();
            replacePrimaryFragment(nextFragment, true);
        }
    }
}
