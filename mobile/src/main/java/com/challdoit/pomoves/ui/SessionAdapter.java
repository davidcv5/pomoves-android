package com.challdoit.pomoves.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.challdoit.pomoves.R;
import com.challdoit.pomoves.data.PomovesProvider;
import com.challdoit.pomoves.model.Session;

/**
 * Created by David on 12/28/14.
 */
public class SessionAdapter extends CursorAdapter {

    public SessionAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.fragment_session_list_item,
                parent,
                false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Session session = new PomovesProvider.SessionCursor(cursor).getSession();
        TextView sessionItem = (TextView) view;
        sessionItem.setText(session.toString());
    }
}
