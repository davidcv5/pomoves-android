package com.challdoit.pomoves.ui;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.challdoit.pomoves.provider.PomovesProvider;
import com.challdoit.pomoves.model.Session;

public class SessionAdapter extends CursorAdapter {

    private static final String TAG = SessionAdapter.class.getSimpleName();
    private static final int VIEW_TYPE_CURRENT = 0;
    private static final int VIEW_TYPE_PAST = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    LayoutInflater mLayoutInflater;

    public SessionAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType = getItemViewType(cursor.getPosition());

        switch (viewType) {
            case VIEW_TYPE_CURRENT:
                return getPomodoroTimerView(context, cursor, parent);
            default:
                return getSessionItemView(context, cursor, parent);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (cursor.getPosition() > 0 && view instanceof TextView) {
            Log.d(TAG, "Binding view at position: " + cursor.getPosition());
            Session session = new PomovesProvider.SessionCursor(cursor).getSession();
            TextView sessionItem = (TextView) view;
            sessionItem.setText(session.toString());
        }
    }

    private View getPomodoroTimerView(final Context context, Cursor cursor, ViewGroup parent) {
        return null;
//        View view = mLayoutInflater.inflate(
//                R.layout.pomodoro_timer,
//                parent,
//                false);
//
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//
//        Point size = new Point();
//        display.getSize(size);
//        int width = size.x;
//        int height = size.y;
//
//        view.getLayoutParams().height = height - 72;
//
//        TextView t = (TextView) view.findViewById(R.id.steps_count_textview);
//        PomovesProvider.SessionCursor sessionCursor = new PomovesProvider.SessionCursor(cursor);
//        t.setText("ID: " + sessionCursor.getSession().getId());
//
//        ImageButton action = (ImageButton) view.findViewById(R.id.actionButton);
//        action.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(context, "some stuff", Toast.LENGTH_SHORT).show();
//            }
//        });
//        return view;
    }

    private View getSessionItemView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
//        return mLayoutInflater.inflate(
//                R.layout.fragment_session_list_item,
//                parent,
//                false);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_CURRENT : VIEW_TYPE_PAST;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
