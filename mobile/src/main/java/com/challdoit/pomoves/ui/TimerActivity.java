package com.challdoit.pomoves.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;

import com.challdoit.pomoves.R;

public class TimerActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setTitle("My Toolbar");
        toolbar.setSubtitle("subtitle");

        toolbar.setNavigationIcon(R.drawable.ic_drawer);

        setSupportActionBar(toolbar);

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(
                R.id.content_frame,
                TimerFragment.newInstance()
        ).commit();

    }
}
