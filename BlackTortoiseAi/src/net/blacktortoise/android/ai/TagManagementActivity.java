
package net.blacktortoise.android.ai;

import java.util.List;

import net.blacktortoise.android.ai.db.DbHelper;
import net.blacktortoise.android.ai.model.TagItemModel;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class TagManagementActivity extends Activity implements OnClickListener {
    private ListView mTagItemModelList;

    private DbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_management);

        findViewById(R.id.addNetTagButton).setOnClickListener(this);

        mTagItemModelList = (ListView)findViewById(R.id.tagItemModelList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        {
            if (mDbHelper != null) {
                mDbHelper.close();
            }
            mDbHelper = new DbHelper(this);
        }
        refleshList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addNetTagButton) {
            Intent intent = new Intent(this, TakeTagActivity.class);
            startActivity(intent);
        }
    }

    private void refleshList() {
        List<TagItemModel> models = mDbHelper.findTagItemModel(false);
        TagItemModelAdapter adapter = new TagItemModelAdapter(this, models);
        mTagItemModelList.setAdapter(adapter);
    }
}
