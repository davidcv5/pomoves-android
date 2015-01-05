package com.challdoit.pomoves.ui;

import android.support.v4.app.Fragment;

public class SessionActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return SessionFragment.newInstance();
    }
}
