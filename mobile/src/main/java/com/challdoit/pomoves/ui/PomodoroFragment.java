package com.challdoit.pomoves.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.challdoit.pomoves.R;
import com.challdoit.pomoves.SessionManager;
import com.challdoit.pomoves.data.PomovesContract;
import com.challdoit.pomoves.data.PomovesProvider.SessionCursor;
import com.challdoit.pomoves.model.Event;
import com.challdoit.pomoves.model.Session;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PomodoroFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PomodoroFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int POMODORO_LOADER = 0;
    private SessionManager mSessionManager;

    private TextView mTypeTextView;
    private TextView mCountTextView;
    private Button mStartButton;


    public static PomodoroFragment newInstance() {
        PomodoroFragment fragment = new PomodoroFragment();
        return fragment;
    }

    public PomodoroFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionManager = SessionManager.get(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pomodoro, container, false);

        mTypeTextView = (TextView) view.findViewById(R.id.typeTextView);
        mCountTextView = (TextView) view.findViewById(R.id.countTextView);
        mStartButton = (Button) view.findViewById(R.id.startButton);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSessionManager.isTrackingSession()) {
                    mSessionManager.stopEvent(true);
                } else {
                    mSessionManager.startSession();
                }
            }
        });

        Button sessionButton = (Button) view.findViewById(R.id.sessionsButton);
        sessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SessionActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(POMODORO_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(POMODORO_LOADER, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri sessionUri = PomovesContract.SessionEntry.buildSessionUri(
                mSessionManager.getCurrentSessionId());

        return new CursorLoader(
                getActivity(),
                sessionUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            SessionCursor sessionCursor = new SessionCursor(data);
            Session session = sessionCursor.getSession();

            mTypeTextView.setText(Event.getName(
                    getActivity(),
                    mSessionManager.getCurrentEventType()) + " " + session.getId());
            mCountTextView.setText(Integer.toString(mSessionManager.getPomodoroCount()));
            if (mSessionManager.isTrackingSession())
                mStartButton.setText("Stop");
            else
                mStartButton.setText("Start");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
