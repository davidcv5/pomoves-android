package com.challdoit.pomoves.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.challdoit.pomoves.SessionManager;
import com.challdoit.pomoves.model.Event;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.SessionReadResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.challdoit.pomoves.util.LogUtils.LOGD;
import static com.challdoit.pomoves.util.LogUtils.LOGI;

public class FitUtils implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = FitUtils.class.getSimpleName();

    private static final int ACTION_START_SESSION = 0;
    private static final int ACTION_STOP_SESSION = 1;
    private static final int ACTION_READ_SESSION = 2;

    private static final String SESSION_ID_PREFIX = "com.challdoit.pomoves.";
    private static final String SESSION_NAME = "Pomoves";
    private static final String SESSION_DESCRIPTION = "Pomodoro Session";

    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";

    private String mAccountName;
    private Context mContext;
    private GoogleApiClient mClient;
    private int mAction;
    private long mSessionId;

    public static final String FIT_SCOPES[] = {
            "https://www.googleapis.com/auth/fitness.activity.write"};
    private OnDataPointListener mListener;

    public FitUtils(Context context, String accountName) {
        LOGD(TAG, "Helper created. Account: " + accountName);
        mContext = context;
        mAccountName = accountName;
    }

    private void buildFitnessClient() {
        mClient = new GoogleApiClient.Builder(mContext)
                .addApi(Fitness.SENSORS_API)
                .addApi(Fitness.SESSIONS_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .setAccountName(mAccountName)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public String getSessionIdentifier() {
        return getSessionIdentifier(mSessionId);
    }

    public String getSessionIdentifier(long id) {
        return SESSION_ID_PREFIX + id;
    }

    public void startSession(long eventId) {
        LOGI(TAG, "Starting session");
        if (mClient == null) {
            LOGI(TAG, "Client is null, so creating it");
            buildFitnessClient();
        }

        mSessionId = eventId;

        if (!mClient.isConnected()) {
            LOGI(TAG, "Client is disconnected... setting action to START and connecting");
            mAction = ACTION_START_SESSION;
            mClient.connect();
        } else {
            LOGI(TAG, "Client is already connected... starting sensors");
            startSensors();
        }
    }

    public void stopSession(long eventId) {
        LOGI(TAG, "Stopping session");
        if (mClient == null) {
            LOGI(TAG, "Client is null, so creating it");
            buildFitnessClient();
        }

        mSessionId = eventId;

        if (!mClient.isConnected()) {
            LOGI(TAG, "Client is disconnected... setting action to STOP and connecting");
            mAction = ACTION_STOP_SESSION;
            mClient.connect();
        } else {
            LOGI(TAG, "Client is already connected... stopping sensors");
            stopSensors();
        }
    }

    public void readSession() {
        LOGI(TAG, "Reading session");
        if (mClient == null) {
            LOGI(TAG, "Client is null, so creating it");
            buildFitnessClient();
        }


        if (!mClient.isConnected()) {
            LOGI(TAG, "Client is disconnected... setting action to READ and connecting");
            mAction = ACTION_READ_SESSION;
            mClient.connect();
        } else {
            LOGI(TAG, "Client is already connected... stopping reading");
            new ReadSessionTask().execute();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        LOGI(TAG, "GoogleApiClient connected! - Action: " + mAction);

        switch (mAction) {
            case ACTION_START_SESSION:
                //startRecording(getSessionIdentifier());
                startSensors();
                break;
            case ACTION_STOP_SESSION:
                //stopRecording(getSessionIdentifier());
                stopSensors();
                break;
            case ACTION_READ_SESSION:
                new ReadSessionTask().execute();
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        LOGD(TAG, "onConnectionSuspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        LOGD(TAG, "onConnectionFailed. " + connectionResult.toString());
    }

    /**
     * Return a {@link SessionReadRequest} for all speed data in the past week.
     */
    private SessionReadRequest buildSessionReadRequest(
            String sessionId, long startTime, long endTime) {
        LOGI(TAG, "Reading History API results for session: " + sessionId);

        // Build a session read request
        return new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setSessionName(SESSION_NAME)
                .build();
    }

    public void saveSession(Event event) {
        new SaveSessionTask(event).execute();
    }

    private class ReadStepsTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            // Setting a start and end date using a range of 1 week before this moment.
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.HOUR, -2);
            long startTime = cal.getTimeInMillis();

            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            LOGI(TAG, "Range Start: " + dateFormat.format(startTime) + " -- " + startTime);
            LOGI(TAG, "Range End: " + dateFormat.format(endTime) + " -- " + endTime);

            DataReadRequest readRequest = new DataReadRequest.Builder()
                    // The data request can specify multiple data types to return, effectively
                    // combining multiple data queries into one call.
                    // In this example, it's very unlikely that the request is for several hundred
                    // datapoints each consisting of a few steps and a timestamp.  The more likely
                    // scenario is wanting to see how many steps were walked per day, for 7 days.
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                            // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                            // bucketByTime allows for a time span, whereas bucketBySession would allow
                            // bucketing by "sessions", which would need to be defined in code.
                    .bucketBySession(10, TimeUnit.SECONDS)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build();


            Fitness.HistoryApi.readData(mClient, readRequest).setResultCallback(new ResultCallback<DataReadResult>() {
                @Override
                public void onResult(DataReadResult dataReadResult) {
                    LOGI(TAG, "DataReadResult: " + dataReadResult.toString());
                    printData(dataReadResult);

                }
            });

            return null;
        }
    }

    public class ReadSessionTask extends AsyncTask<Void, Void, Void> {

        private long startTime;
        private long endTime;

        private ReadSessionTask() {
            // Setting a start and end date using a range of 2 hours before this moment.
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            endTime = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_MONTH, -5);
            startTime = cal.getTimeInMillis();
        }

        private ReadSessionTask(Session session) {
            startTime = session.getStartTime(TimeUnit.MILLISECONDS);
            endTime = session.getEndTime(TimeUnit.MILLISECONDS);
        }

        protected Void doInBackground(Void... params) {

            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            LOGI(TAG, "Range Start: " + dateFormat.format(startTime) + " -- " + startTime);
            LOGI(TAG, "Range End: " + dateFormat.format(endTime) + " -- " + endTime);

            SessionReadRequest sessionReadRequest =
                    buildSessionReadRequest(getSessionIdentifier(), startTime, endTime);

            SessionReadResult sessionReadResult =
                    Fitness.SessionsApi.readSession(mClient, sessionReadRequest)
                            .await(1, TimeUnit.MINUTES);

            int steps = 0;

            // Get a list of the sessions that match the criteria to check the result.
            LOGI(TAG, "Session read was successful. Number of returned sessions is: "
                    + sessionReadResult.getSessions().size());
            for (Session session : sessionReadResult.getSessions()) {
                // Process the session
                dumpSession(session);

                // Process the data sets for this session
                List<DataSet> dataSets = sessionReadResult.getDataSet(session);
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                    for (DataPoint dp : dataSet.getDataPoints()) {
                        steps += dp.getValue(Field.FIELD_STEPS).asInt();
                    }
                }
            }

            //SessionManager.get(mContext).updateSteps(steps);

            return null;
        }
    }

    private class SaveSessionTask extends AsyncTask<Void, Void, Void> {

        private Event mEvent;

        public SaveSessionTask(Event event) {
            mEvent = event;
        }

        @Override
        protected Void doInBackground(Void... params) {

            LOGI(TAG, "Saving event...");

            if (mEvent == null) {
                LOGI(TAG, "Event is null... returning");
                return null;
            }

            long startDate = mEvent.getStartDate().getTime();
            long endDate = mEvent.getEndDate().getTime();

            DataSource stepsDataSource = new DataSource.Builder()
                    .setAppPackageName(mContext.getPackageName())
                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .setName(SESSION_NAME + " - " + Event.getName(mContext, mEvent.getEventType()))
                    .setType(DataSource.TYPE_RAW)
                    .build();

            DataSet stepsDataSet = DataSet.create(stepsDataSource);

            DataPoint stepsDp = stepsDataSet.createDataPoint()
                    .setTimeInterval(
                            startDate,
                            endDate,
                            TimeUnit.MILLISECONDS);
            stepsDp.getValue(Field.FIELD_STEPS).setInt(mEvent.getData().steps);
            stepsDataSet.add(stepsDp);


            // Create a session with metadata about the activity.
            Session session = new Session.Builder()
                    .setName(SESSION_NAME + " - " + Event.getName(mContext, mEvent.getEventType()))
                    .setDescription(SESSION_DESCRIPTION)
                    .setIdentifier(getSessionIdentifier(mEvent.getStartDate().getTime()))
                    .setActivity(FitnessActivities.WALKING)
                    .setStartTime(startDate, TimeUnit.MILLISECONDS)
                    .setEndTime(endDate, TimeUnit.MILLISECONDS)
                    .build();

            // Build a session insert request
            SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                    .setSession(session)
                    .addDataSet(stepsDataSet)
                    .build();

            // Then, invoke the Sessions API to insert the session and await the result,
            // which is possible here because of the AsyncTask. Always include a timeout when
            // calling await() to avoid hanging that can occur from the service being shutdown
            // because of low memory or other conditions.
            LOGI(TAG, "Inserting the session in the History API");

            com.google.android.gms.common.api.Status insertStatus =
                    Fitness.SessionsApi.insertSession(mClient, insertRequest)
                            .await(1, TimeUnit.MINUTES);

            // Before querying the session, check to see if the insertion succeeded.
            if (!insertStatus.isSuccess())

            {
                LOGI(TAG, "There was a problem inserting the session: " +
                        insertStatus.getStatusMessage());
                return null;
            }

            // At this point, the session has been inserted and can be read.
            LOGI(TAG, "Session insert was successful!");
            return null;
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        LOGI(TAG, "Data returned for Data type: " + dataSet.getDataType().getName() + " " + dataSet.toString());
        for (DataPoint dp : dataSet.getDataPoints()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            LOGI(TAG, "Data point:");
            LOGI(TAG, "\tType: " + dp.getDataType().getName());
            LOGI(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            LOGI(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                LOGI(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

    private void dumpSession(Session session) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        LOGI(TAG, "Data returned for Session: " + session.getName()
                + "\n\tDescription: " + session.getDescription()
                + "\n\tData: " + session.toString()
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }

    private void printData(DataReadResult dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            LOGI(TAG, "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                LOGI(TAG, "Number of returned DataSets is: "
                        + dataSets.size());
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            LOGI(TAG, "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
        // [END parse_read_data_result]
    }


    public void startSensors() {

        mListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                SessionManager.get(mContext)
                        .updateSteps(dataPoint.getValue(Field.FIELD_STEPS).asInt());
            }
        };

        Fitness.SensorsApi.add(
                mClient,
                new SensorRequest.Builder()
                        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .setSamplingRate(1, TimeUnit.SECONDS)
                        .build(),
                mListener
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    LOGI(TAG, "Listener registered!");
                } else {
                    LOGI(TAG, "Listener not registered.");
                }
            }
        });
    }

    public void stopSensors() {
        if (mListener != null)
            Fitness.SensorsApi.remove(mClient, mListener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                LOGI(TAG, "Listener was removed!");
                            } else {
                                LOGI(TAG, "Listener was not removed.");
                            }
                        }
                    });
    }
}
