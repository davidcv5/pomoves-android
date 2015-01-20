package com.challdoit.pomoves.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.challdoit.pomoves.R;
import com.challdoit.pomoves.SessionManager;

import java.util.concurrent.TimeUnit;

public class TimerFragment extends Fragment {

    private static final String TAG = TimerFragment.class.getSimpleName();
    private SessionManager mSessionManager;
    private PomodoroCountdown mPomodoroCountdown;

    private TextView mTimerText;
    private FloatingActionButton mTimerButton;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPomodoroCountdown != null)
                mPomodoroCountdown.cancel();
            boolean isRunning = mSessionManager.isTrackingSession();
            if (isRunning) {
                mPomodoroCountdown = new PomodoroCountdown(
                        mSessionManager.getCurrentEndTime() - System.currentTimeMillis(), 1000);
                mPomodoroCountdown.start();
            }
            if (mTimerButton != null)
                mTimerButton.setChecked(isRunning);
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        mTimerText = (TextView) view.findViewById(R.id.timerTextView);

        setupCountdown();

        mTimerButton = (FloatingActionButton) view.findViewById(R.id.timerButton);

        mTimerButton.setChecked(mSessionManager.isTrackingSession());

        mTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        ((FloatingActionButton) v).isChecked() ?
                                SessionManager.ACTION_NEXT :
                                SessionManager.ACTION_STOP);
                getActivity().sendBroadcast(intent);
            }
        });

        return view;
    }

    private void setupCountdown() {

        long timeRemaining = mSessionManager.getCurrentEndTime() - System.currentTimeMillis();

        mPomodoroCountdown = new PomodoroCountdown(timeRemaining, 1000);

        if (mSessionManager.isTrackingSession()) {
            mTimerText.setText(getFormattedTime(timeRemaining));
            mPomodoroCountdown.start();
        } else
            mTimerText.setText(getFormattedTime(0));
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
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemaining - minutes * 60);

        return String.format("%02d:%02d", minutes, seconds);
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
}
