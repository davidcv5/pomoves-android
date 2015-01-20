package com.challdoit.pomoves.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.challdoit.pomoves.R;


public class StatsFragment extends Fragment {


    public static StatsFragment newInstance() {
        StatsFragment fragment = new StatsFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        return view;
    }


}
