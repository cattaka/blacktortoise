
package com.example.ultimatetank;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.example.ultimatetank.fragment.BaseFragment.IBaseFragmentAdapter;

public class MainActivity extends Activity implements IBaseFragmentAdapter {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
