
package net.blacktortoise.android.ai;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tagTrackerButton).setOnClickListener(this);
        findViewById(R.id.debugButton).setOnClickListener(this);
        findViewById(R.id.tagManagementButtion).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tagTrackerButton) {
            Intent intent = new Intent(this, TagTrackerActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.debugButton) {
            Intent intent = new Intent(this, DebugActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.tagManagementButtion) {
            Intent intent = new Intent(this, TagManagementActivity.class);
            startActivity(intent);
        }
    }
}
