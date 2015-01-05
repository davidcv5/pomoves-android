package com.challdoit.pomoves.ui;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.challdoit.pomoves.R;
import com.challdoit.pomoves.SessionManager;
import com.challdoit.pomoves.data.PomovesContract;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PomodoroFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PomodoroFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = PomodoroFragment.class.getSimpleName();

    private static final int POMODORO_LOADER = 0;
    private SessionManager mSessionManager;
    private SessionAdapter mSessionAdapter;


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
        mSessionAdapter = new SessionAdapter(getActivity(), null, 0);

        ListView sessionListView = (ListView) view.findViewById(R.id.pomodoro_list_view);
        sessionListView.setAdapter(mSessionAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(POMODORO_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(POMODORO_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
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
        Uri sessionUri = PomovesContract.SessionEntry.CONTENT_URI;

        return new CursorLoader(
                getActivity(),
                sessionUri,
                null,
                null,
                null,
                PomovesContract.SessionEntry.COLUMN_DATE_TEXT + " DESC ");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            Log.d(TAG, "onLoadFinished - count: " + data.getCount());
            mSessionAdapter.swapCursor(data);
        } else {
            Log.d(TAG, "onLoadFinished - data is null");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSessionAdapter.swapCursor(null);
    }
}
