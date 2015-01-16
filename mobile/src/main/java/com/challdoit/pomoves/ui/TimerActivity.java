package com.challdoit.pomoves.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;

import com.challdoit.pomoves.R;

public class TimerActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        toolbar.setNavigationIcon(R.drawable.ic_drawer);

        setSupportActionBar(toolbar);

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(
                R.id.content_frame,
                TimerFragment.newInstance()
        ).commit();

        NavigationDrawerFragment drawer =
                (NavigationDrawerFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.navigation_fragment);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setUp(drawerLayout, toolbar);
    }
}
