package com.challdoit.pomoves.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.challdoit.pomoves.R;
import com.challdoit.pomoves.util.UIUtils;

public class StatsActivity extends BaseActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stats);

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(
                R.id.content_frame,
                StatsFragment.newInstance()
        ).commit();

    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_STATS;
    }
}
