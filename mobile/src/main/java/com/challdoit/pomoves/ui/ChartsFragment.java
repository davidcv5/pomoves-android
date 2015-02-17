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

import com.challdoit.pomoves.R;
import com.challdoit.pomoves.data.PomovesContract;
import com.challdoit.pomoves.model.Session;
import com.challdoit.pomoves.provider.PomovesProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SelectedValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

import static com.challdoit.pomoves.util.LogUtils.LOGI;

public class ChartsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ChartsFragment.class.getSimpleName();
    private static final int DATA_TYPE_POMODORO = 0;
    private static final int DATA_TYPE_STEPS = 1;
    private static final int DATA_TYPE_WATER = 2;
    private ColumnChartView pomodoroChart;
    private ColumnChartView stepsChart;
//    private ColumnChartView waterChart;

    private static final int COLUMN_COUNT = 14;

    private static final int SESSION_LOADER = 0;

    public ChartsFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(SESSION_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_charts, container, false);

        pomodoroChart = (ColumnChartView) rootView.findViewById(R.id.pomodoroChart);
        stepsChart = (ColumnChartView) rootView.findViewById(R.id.stepsChart);
//        waterChart = (ColumnChartView) rootView.findViewById(R.id.waterChart);

        pomodoroChart.setOnValueTouchListener(new ValueTouchListener());
        stepsChart.setOnValueTouchListener(new ValueTouchListener());
//        waterChart.setOnValueTouchListener(new ValueTouchListener());


        return rootView;
    }

    private void generateData(
            int dataType,
            ColumnChartView chart,
            List<Session> sessions,
            String columnName,
            int columnColor) {
        int numColumns = COLUMN_COUNT;

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.add(Calendar.DATE, -COLUMN_COUNT + 1);

        SimpleDateFormat dayShortFormat = new SimpleDateFormat("E dd", Locale.US);
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);


        List<AxisValue> axisValues = new ArrayList<>();
        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;

        int sessionIndex = 0;
        Session currentSession = null;

        for (int i = 0; i < numColumns; ++i) {

            cal.add(Calendar.DATE, 1);
            axisValues.add(new AxisValue(i, dayShortFormat.format(cal.getTime()).toCharArray()));

            if (sessions.size() > sessionIndex
                    && dayFormat.format(sessions.get(sessionIndex).getDate())
                    .equals(dayFormat.format(cal.getTime()))) {
                currentSession = sessions.get(sessionIndex);
                sessionIndex++;
            } else {
                currentSession = null;
            }

            int value = 0;
            if (currentSession != null) {
                switch (dataType) {
                    case DATA_TYPE_POMODORO:
                        value = currentSession.getStats().pomoCount;
                        break;
                    case DATA_TYPE_STEPS:
                        value = currentSession.getStats().stepCount;
                        break;
//                    case DATA_TYPE_WATER:
//                        value = currentSession.getStats().waterCount;
//                        break;
                }
            }

            values = new ArrayList<>();
            values.add(new SubcolumnValue(value, getResources().getColor(columnColor)));

            Column column = new Column(values);
            column.setHasLabels(true);
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);
        }

        ColumnChartData data;

        data = new ColumnChartData(columns);

        Axis axisX = new Axis(axisValues);
        axisX.setName(columnName);
        data.setAxisXBottom(axisX);

        Axis axisY = new Axis().setHasLines(true);
        data.setAxisYLeft(axisY);

        chart.setValueSelectionEnabled(true);
        chart.setZoomEnabled(false);
        //        chart.setZoomType(ZoomType.HORIZONTAL);
        //        chart.setZoomLevelWithAnimation(2, 0, 2);
        chart.setColumnChartData(data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.add(Calendar.WEEK_OF_MONTH, -2);

        Uri sessionUri = PomovesContract.SessionEntry.buildSessionWithStartDate(
                PomovesContract.getDbDateString(cal.getTime()));

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
        List<Session> sessions = new ArrayList<>();
        while (data.moveToNext()) {
            Session session = new PomovesProvider.SessionCursor(data)
                    .getSession();
            sessions.add(session);

            LOGI(TAG, "Session found: " + session.toString());
        }

        generateData(DATA_TYPE_POMODORO, pomodoroChart, sessions, "Pomodoro", R.color.pomodoro_count);
        generateData(DATA_TYPE_STEPS, stepsChart, sessions, "Steps", R.color.steps_count);
//        generateData(DATA_TYPE_WATER, waterChart, sessions, "Water", R.color.water_count);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class ValueTouchListener implements ColumnChartOnValueSelectListener {

        @Override
        public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
            SelectedValue selectedValue = new SelectedValue(
                    columnIndex, subcolumnIndex, SelectedValue.SelectedValueType.COLUMN);
            if (pomodoroChart.getSelectedValue().getFirstIndex() != selectedValue.getFirstIndex())
                pomodoroChart.selectValue(selectedValue);
            if (stepsChart.getSelectedValue().getFirstIndex() != selectedValue.getFirstIndex())
                stepsChart.selectValue(selectedValue);
//            if (waterChart.getSelectedValue().getFirstIndex() != selectedValue.getFirstIndex())
//                waterChart.selectValue(selectedValue);
        }

        @Override
        public void onValueDeselected() {
            SelectedValue pomodoroSelected = pomodoroChart.getSelectedValue();
            SelectedValue stepsSelected = stepsChart.getSelectedValue();
//            SelectedValue waterSelected = waterChart.getSelectedValue();
            if (pomodoroSelected.isSet()) {
                pomodoroSelected.clear();
                pomodoroChart.selectValue(pomodoroSelected);
            }

            if (stepsSelected.isSet()) {
                stepsSelected.clear();
                stepsChart.selectValue(stepsSelected);
            }

//            if (waterSelected.isSet()) {
//                waterSelected.clear();
//                waterChart.selectValue(waterSelected);
//            }
        }
    }
}
