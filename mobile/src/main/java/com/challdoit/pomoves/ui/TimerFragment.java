package com.challdoit.pomoves.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.challdoit.pomoves.PomovesReceiver;
import com.challdoit.pomoves.R;
import com.challdoit.pomoves.SessionManager;
import com.challdoit.pomoves.Utils;
import com.challdoit.pomoves.data.PomovesContract;
import com.challdoit.pomoves.model.Event;
import com.challdoit.pomoves.model.Session;
import com.challdoit.pomoves.provider.PomovesProvider;

import java.util.concurrent.TimeUnit;

public class TimerFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = TimerFragment.class.getSimpleName();
    private SessionManager mSessionManager;
    private PomodoroCountdown mPomodoroCountdown;

    private FrameLayout mTimerFrameLayout;

    private TextView mTimerText;
    private TextView mPomodoroTextView;
    private TextView mStepsTextView;
    //private TextView mWaterTextView;
    private FloatingActionButton mTimerButton;

    private static final int POMODORO_LOADER = 0;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupCountdown();

            if (mTimerButton != null)
                mTimerButton.setChecked(mSessionManager.isTrackingSession());

            updateColors();
        }
    };

    public static TimerFragment newInstance() {
        TimerFragment fragment = new TimerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionManager = SessionManager.get(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(POMODORO_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        mTimerFrameLayout = (FrameLayout) view.findViewById(R.id.timerFrameLayout);

        mTimerText = (TextView) view.findViewById(R.id.timerTextView);
        mPomodoroTextView = (TextView) view.findViewById(R.id.pomodoro_countTextView);
        mStepsTextView = (TextView) view.findViewById(R.id.stepsTextView);
//        mWaterTextView = (TextView) view.findViewById(R.id.waterTextView);

        mTimerButton = (FloatingActionButton) view.findViewById(R.id.timerButton);

        mTimerButton.setChecked(mSessionManager.isTrackingSession());

        mTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        ((FloatingActionButton) v).isChecked() ?
                                SessionManager.ACTION_NEXT :
                                SessionManager.ACTION_STOP);
                intent.putExtra(PomovesReceiver.DISABLE_VIBRATION, true);

                getActivity().sendBroadcast(intent);
            }
        });

        return view;
    }

    private void setupCountdown() {

        long timeRemaining = mSessionManager.getCurrentEndTime() - System.currentTimeMillis();

        if (mPomodoroCountdown != null)
            mPomodoroCountdown.cancel();
        mPomodoroCountdown = new PomodoroCountdown(timeRemaining, 1000);

        if (mSessionManager.isTrackingSession()) {
            mTimerText.setText(getFormattedTime(timeRemaining));
            mPomodoroCountdown.start();
        } else {
            int defaultDuration = mSessionManager.getEventDuration(mSessionManager.getCurrentEventType());
            mTimerText.setText(getFormattedTime(defaultDuration * 1000 * 60));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        setupCountdown();

        if (mTimerButton != null)
            mTimerButton.setChecked(mSessionManager.isTrackingSession());

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mBroadcastReceiver,
                new IntentFilter(SessionManager.ACTION_EVENT));

        updateColors();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                mBroadcastReceiver);
        mPomodoroCountdown.cancel();
    }

    private String getFormattedTime(long timeRemaining) {
        if (timeRemaining <= 0)
            return "00:00";
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemaining);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemaining) - minutes * 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        long sessionId = mSessionManager.getCurrentSessionId();

        Uri sessionUri = PomovesContract.SessionEntry.buildSessionUri(sessionId);

        return new CursorLoader(
                getActivity(),
                sessionUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            Session session =
                    new PomovesProvider.SessionCursor(cursor).getSession();
            mPomodoroTextView.setText(Integer.toString(session.getStats().pomoCount));
            mStepsTextView.setText(Integer.toString(session.getStats().stepCount));
//            mWaterTextView.setText(Integer.toString(session.getStats().waterCount));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private class PomodoroCountdown extends CountDownTimer {

        public PomodoroCountdown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mTimerText.setText(getFormattedTime(millisUntilFinished));
        }

        @Override
        public void onFinish() {
            Log.d(TAG, "countdown timer finished");
        }
    }

    @SuppressLint("NewApi")
    private void updateColors() {

        Toolbar toolbar = ((BaseActivity) getActivity()).getActionBarToolbar();
        toolbar.setBackgroundColor(getResources().getColor(
                mSessionManager.getCurrentEventType() == Event.POMODORO ?
                        R.color.pomodoro_toolbar :
                        R.color.break_toolbar
        ));

        mTimerFrameLayout.setBackgroundColor(getResources().getColor(
                mSessionManager.getCurrentEventType() == Event.POMODORO ?
                        R.color.pomodoro_timer_frame :
                        R.color.break_timer_frame
        ));


        if (Utils.hasLollipopApi()) {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(
                    mSessionManager.getCurrentEventType() == Event.POMODORO ?
                            R.color.pomodoro_status_bar :
                            R.color.break_status_bar));
        }
    }
}
