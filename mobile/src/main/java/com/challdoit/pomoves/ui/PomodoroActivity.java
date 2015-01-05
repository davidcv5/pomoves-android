package com.challdoit.pomoves.ui;

import android.support.v4.app.Fragment;

/**
 * Created by admin on 12/29/14.
 */
public class PomodoroActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return PomodoroFragment.newInstance();
    }
}
