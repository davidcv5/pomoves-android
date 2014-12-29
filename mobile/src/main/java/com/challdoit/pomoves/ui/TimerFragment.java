package com.challdoit.pomoves.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.challdoit.pomoves.R;

public class TimerFragment extends Fragment {

    public static TimerFragment newInstance() {
        TimerFragment fragment = new TimerFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);


        return view;
    }
}
