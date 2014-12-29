package com.challdoit.pomoves.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.challdoit.pomoves.R;

/**
 * Created by David on 12/28/14.
 */
public class SessionActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, SessionFragment.newInstance())
                    .commit();
        }
    }
}
