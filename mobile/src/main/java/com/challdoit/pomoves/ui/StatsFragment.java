package com.challdoit.pomoves.ui;


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
import android.widget.ListView;

import com.challdoit.pomoves.R;
import com.challdoit.pomoves.data.PomovesContract;


public class StatsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private SessionAdapter mSessionAdapter;
    private ListView mListView;


    private static final int SESSION_LOADER = 0;

    public static StatsFragment newInstance() {
        StatsFragment fragment = new StatsFragment();
        return fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(SESSION_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        mSessionAdapter = new SessionAdapter(getActivity(), null, 0);

        mListView = (ListView) view.findViewById(R.id.session_listview);
        mListView.setAdapter(mSessionAdapter);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = PomovesContract.SessionEntry._ID + " DESC";

        Uri sessionUri = PomovesContract.SessionEntry.CONTENT_URI;

        return new CursorLoader(
                getActivity(),
                sessionUri,
                null,
                null,
                null,
                sortOrder);
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
