package com.challdoit.pomoves.util;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.google.android.gms.fitness.result.SessionStopResult;

import java.lang.ref.WeakReference;
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

    private static final String SESSION_NAME = "Pomoves";
    private static final String SESSION_IDENTIFIER = "com.challdoit.pomoves.SESSION_IDENTIFIER";

    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";

    private String mAccountName;
    private WeakReference<Callbacks> mCallbacksRef;
    private Context mContext;
    private GoogleApiClient mClient;
    private int mAction;

    public static final String FIT_SCOPES[] = {
            "https://www.googleapis.com/auth/fitness.activity.write"};

    public interface Callbacks {
        void onSessionStarted(String accountName);

        void onSessionStopped(String accountName);
    }

    public FitUtils(Context context, Callbacks callbacks, String accountName) {
        LOGD(TAG, "Helper created. Account: " + accountName);
        mCallbacksRef = new WeakReference<>(callbacks);
        mContext = context;
        mAccountName = accountName;
    }

    private void buildFitnessClient() {
        mClient = new GoogleApiClient.Builder(mContext)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .setAccountName(mAccountName)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void startSession() {
        LOGI(TAG, "Starting session");
        if (mClient == null) {
            LOGI(TAG, "Client is null, so creating it");
            buildFitnessClient();
        }


        if (!mClient.isConnected()) {
            LOGI(TAG, "Client is disconnected... setting action to START and connecting");
            mAction = ACTION_START_SESSION;
            mClient.connect();
        } else {
            LOGI(TAG, "Client is already connected... starting recording");
            startRecording();
        }
    }

    private void startRecording() {
        LOGI(TAG, "Start recording");
        Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                LOGI(TAG, "Existing subscription for activity detected.");
                            } else {
                                LOGI(TAG, "Successfully subscribed!");
                            }
                        } else {
                            LOGI(TAG, "There was a problem subscribing.");
                        }
                    }
                });

        Session session = new Session.Builder()
                .setName(SESSION_NAME)
                .setIdentifier(SESSION_IDENTIFIER)
                .setStartTime(new Date().getTime(), TimeUnit.MILLISECONDS)
                .build();

        Fitness.SessionsApi.startSession(mClient, session)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        LOGI(TAG, "Session recording started");
                    }
                });
    }

    public void stopSession() {
        LOGI(TAG, "Stopping session");
        if (mClient == null) {
            LOGI(TAG, "Client is null, so creating it");
            buildFitnessClient();
        }


        if (!mClient.isConnected()) {
            LOGI(TAG, "Client is disconnected... setting action to STOP and connecting");
            mAction = ACTION_STOP_SESSION;
            mClient.connect();
        } else {
            LOGI(TAG, "Client is already connected... stopping recording");
            stopRecording();
        }
    }

    private void stopRecording() {
        LOGI(TAG, "Stop recording");
        Fitness.SessionsApi.stopSession(mClient, SESSION_IDENTIFIER)
                .setResultCallback(new ResultCallback<SessionStopResult>() {
                    @Override
                    public void onResult(SessionStopResult sessionStopResult) {
                        LOGI(TAG, "Session recording stopped");
                    }
                });

        Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            LOGI(TAG, "Successfully unsubscribed for data type: " +
                                    DataType.TYPE_STEP_COUNT_DELTA.getName());
                        } else {
                            // Subscription not removed
                            LOGI(TAG, "Failed to unsubscribe for data type: " +
                                    DataType.TYPE_STEP_COUNT_DELTA.getName());
                        }
                    }
                });
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
            startReading();
        }
    }

    private void startReading() {


        // Begin by creating the query.
        SessionReadRequest readRequest = buildSessionReadRequest();

        // Invoke the Sessions API to fetch the session with the query and wait for the result
        // of the read request.
        SessionReadResult sessionReadResult =
                Fitness.SessionsApi.readSession(mClient, readRequest)
                        .await(1, TimeUnit.MINUTES);

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
            }
        }
    }

    /**
     * Return a {@link SessionReadRequest} for all speed data in the past week.
     */
    private SessionReadRequest buildSessionReadRequest() {
        LOGI(TAG, "Reading History API results for session: " + SESSION_NAME);
        // Set a start and end time for our query, using a start time of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        // Build a session read request
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_SPEED)
                .setSessionName(SESSION_NAME)
                .build();

        return readRequest;
    }

    @Override
    public void onConnected(Bundle bundle) {

        LOGI(TAG, "GoogleApiClient connected! - Action: " + mAction);

        switch (mAction) {
            case ACTION_START_SESSION:
                startRecording();
                break;
            case ACTION_STOP_SESSION:
                stopRecording();
            case ACTION_READ_SESSION:
                startReading();
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

    private void dumpDataSet(DataSet dataSet) {
        LOGI(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
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
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }
}
