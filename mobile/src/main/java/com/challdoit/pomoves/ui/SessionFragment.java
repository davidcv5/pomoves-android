package com.challdoit.pomoves.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.challdoit.pomoves.R;
import com.challdoit.pomoves.data.PomovesContract;

public class SessionFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SESSION_LOADER = 0;

    SessionAdapter mSessionAdapter;

    public static SessionFragment newInstance() {
        SessionFragment fragment = new SessionFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mSessionAdapter = new SessionAdapter(getActivity(), null, 0);

        View view = inflater.inflate(R.layout.fragment_session_list, null, false);
        ListView list = (ListView) view.findViewById(R.id.list);
        list.setAdapter(mSessionAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(SESSION_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                PomovesContract.SessionEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSessionAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSessionAdapter.swapCursor(null);
    }

}
