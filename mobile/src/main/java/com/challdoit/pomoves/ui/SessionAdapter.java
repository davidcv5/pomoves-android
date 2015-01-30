package com.challdoit.pomoves.ui;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.challdoit.pomoves.R;
import com.challdoit.pomoves.model.Session;
import com.challdoit.pomoves.provider.PomovesProvider;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class SessionAdapter extends CursorAdapter {
    private static final String TAG = SessionAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;


    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder {
        private final TextView textView;

        public ViewHolder(View v) {
            textView = (TextView) v.findViewById(R.id.dateTextView);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    public SessionAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_session_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_session;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        Session session = new PomovesProvider.SessionCursor(cursor).getSession();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                viewHolder.textView.setText(
                        "A - " + session.toString()
                                + " - " + session.getStats().pomoCount);
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                viewHolder.textView.setText("B - " + session.toString());
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}