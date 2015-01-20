package com.challdoit.pomoves.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.challdoit.pomoves.R;
import com.challdoit.pomoves.util.UIUtils;

public class TimerActivity extends BaseActivity {

    private int mHeaderColor = 0; // 0 means not customized

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timer);

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(
                R.id.content_frame,
                TimerFragment.newInstance()
        ).commit();

        updateHeaderColor();
    }

    private void updateHeaderColor() {
        mHeaderColor = 0;
        findViewById(R.id.toolbar_actionbar).setBackgroundColor(
                mHeaderColor == 0
                        ? getResources().getColor(R.color.theme_primary)
                        : mHeaderColor);
        setNormalStatusBarColor(
                mHeaderColor == 0
                        ? getThemedStatusBarColor()
                        : UIUtils.scaleColor(mHeaderColor, 0.8f, false));
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_TIMER;
    }
}
