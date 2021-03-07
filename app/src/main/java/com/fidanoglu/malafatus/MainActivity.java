package com.fidanoglu.malafatus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button sessionButton;
    //    private Button resetButton;
    private TextView previousSessionData, durationHistory;
    private LineChart history;
//    private Toolbar toolbar;

    private Boolean sessionActive = false;
    private Boolean firstSession = true;

    public static Boolean profileJustCreated = false;

    public DateTimeFormatter dformat = DateTimeFormatter.ofPattern("mm:ss");
    ZonedDateTime startTime;
    long sessionDuration = 0;

    CountThread thread;

    SessionsDatabase db;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setTitle("Dashboard");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(authStateListener);

        if (firebaseAuth.getCurrentUser() != null && !profileJustCreated) {
            Intent intent = new Intent(MainActivity.this, EnterPinActivity.class);
            startActivity(intent);
        }

        db = SessionsDatabase.getAppDatabase(this);

        previousSessionData = findViewById(R.id.previousSession);
        sessionButton = findViewById(R.id.session);
//        resetButton = findViewById(R.id.resetGraph);
        durationHistory = findViewById(R.id.durationHistoryInfo);
//        insights = findViewById(R.id.insights);
        history = findViewById(R.id.durationHistory);

        history.setNoDataText("Tap \"Start session\" to start saving data!");
        history.setNoDataTextColor(R.color.colorAccent);

        previousSessionData.setVisibility(View.GONE);
        updateHistory(history);

        sessionButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (!sessionActive) {
                    thread = new CountThread();
                    sessionActive = true;
                    thread.start();
                    startTime = ZonedDateTime.now();
                    sessionButton.setBackgroundResource(R.drawable.modern_button_red);
                    sessionButton.setText("End session   |   00:00");
                } else {
                    thread.interrupt();
                    sessionActive = false;
                    sessionButton.setBackgroundResource(R.drawable.modern_button_blue);
                    sessionButton.setText("Start session");

                    if (sessionDuration != 0) {

                        if (firstSession) {
                            previousSessionData.setVisibility(View.VISIBLE);
                            slideView(previousSessionData, 0, previousSessionData.getLayoutParams().height);
                            firstSession = false;
                        }

                        updatePreviousSessionInfo();
                        writeDuration(false);
                        updateHistory(history);
                    }
                }
            }
        });

//        resetButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                writeDuration(true);
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_menu) {
            firebaseAuth.signOut();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

            if (firebaseUser == null) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("SessionActive", sessionActive);
        if (sessionActive)
            outState.putString("StartTime", startTime.toString());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle inState) {
        super.onRestoreInstanceState(inState);
        sessionActive = inState.getBoolean("SessionActive");

        if (sessionActive) {
            startTime = ZonedDateTime.parse(inState.getString("StartTime"));
            thread = new CountThread();
            thread.start();
            sessionButton.setText("End session   |         ");
            sessionButton.setBackgroundResource(R.drawable.modern_button_red);
        }
    }

    public void updatePreviousSessionInfo() {
        String sessionInfo;

        long seconds = (sessionDuration / 1000) % 60;
        long minute = (sessionDuration / (1000 * 60)) % 60;
        long hour = (sessionDuration / (1000 * 60 * 60)) % 24;

        @SuppressLint("DefaultLocale") String duration = String.format("%02d:%02d", hour, minute);

        if (minute == 0 && hour == 0) {
            if (seconds == 1)
                sessionInfo = "Last session lasted one second. </br><small>That's pretty short, it won't be saved.</small>";
            else
                sessionInfo = "Last session lasted " + seconds + " seconds. <br/><small>That's pretty short, it won't be saved.</small>";
        } else if (hour == 0 && minute == 1)
            sessionInfo = "Last session lasted " + minute + " minute.";
        else if (hour == 0)
            sessionInfo = "Last session lasted " + minute + " minutes.";
        else
            sessionInfo = "Last session lasted " + duration + ".";
        previousSessionData.setText(Html.fromHtml(sessionInfo, Html.FROM_HTML_MODE_LEGACY));
    }

    public void writeDuration(boolean reset) {
        if (sessionDuration > 60000 && !reset) {
            Session newSession = new Session();
            newSession.setStartTimeValue(startTime.toString());
            newSession.setDuration(sessionDuration);
            db.sessionDao().insert(newSession);
        } else if (reset)
            db.sessionDao().clear();
    }

    public void updateHistory(LineChart history) {
        List<Session> sessions = db.sessionDao().fetchAllData();
        if (sessions.size() > 0) {
            long[] durations = new long[sessions.size()];
            for (int i = 0; i < sessions.size(); i++)
                durations[i] = sessions.get(i).getDuration();
            String finalDurationHistory = "Previous sessions <br/><small>";
            long maxMilliseconds = createChart(durations, history);
            long minute = (maxMilliseconds / (1000 * 60));
            finalDurationHistory += "Longest session: " + minute + " minutes</small>";
            durationHistory.setText(Html.fromHtml(finalDurationHistory, Html.FROM_HTML_MODE_LEGACY));
        } else {
            durationHistory.setText(Html.fromHtml("Previous sessions <br/>" +
                    "<small>You haven't saved any sessions yet.", Html.FROM_HTML_MODE_LEGACY));
        }
    }

    public long createChart(long[] data, LineChart history) {
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < data.length; i++)
            entries.add(new Entry(i, data[i]));

        LineDataSet dataSet = new LineDataSet(entries, "Durations");
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setValueTextColor(R.color.colorAccent);
        dataSet.setCircleColor(R.color.colorAccent);
        dataSet.setLineWidth(3);
        dataSet.setCircleRadius(5);
        dataSet.setCircleHoleRadius(2);
        dataSet.setColor(R.color.colorAccent);
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(ContextCompat.getDrawable(this, R.drawable.graph_fill_gradient));

        LineData lineData = new LineData(dataSet);
        history.setData(lineData);
        history.invalidate();

        history.getAxisLeft().setEnabled(false);
        history.getAxisRight().setEnabled(false);
        history.getXAxis().setEnabled(false);
        history.setDrawMarkers(false);
        history.setDrawBorders(false);
        history.disableScroll();
        history.getLegend().setEnabled(false);
        history.getDescription().setEnabled(false);
        history.setTouchEnabled(false);

        Arrays.sort(data);
        if (data.length > 0) {
            history.getAxisLeft().setAxisMaximum(data[data.length - 1] + 5000);
            history.getAxisLeft().setAxisMinimum(data[0] - (data[data.length - 1] - data[0]) / 4f);
            return data[data.length - 1];
        } else return 0;
    }

    public class CountThread extends Thread {
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(1000);  //1000ms = 1 sec
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sessionDuration = ChronoUnit.MILLIS.between(startTime, ZonedDateTime.now());
                            String sessionText = "End session   |   " +
                                    dformat.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(sessionDuration), ZoneId.systemDefault()));
                            sessionButton.setText(sessionText);
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void slideView(final View view, int currentHeight, int newHeight) {
        // set the values we want to animate between and how long it takes
// to run
        ValueAnimator slideAnimator = ValueAnimator
                .ofInt(currentHeight, newHeight)
                .setDuration(300);


// we want to manually handle how each tick is handled so add a
// listener
        slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // get the value the interpolator is at
                // I'm going to set the layout's height 1:1 to the tick
                view.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                // force all layouts to see which ones are affected by
                // this layouts height change
                view.requestLayout();
            }
        });

// create a new animationset
        AnimatorSet set = new AnimatorSet();
// since this is the only animation we are going to run we just use
// play
        set.play(slideAnimator);
// this is how you set the parabola which controls acceleration
        set.setInterpolator(new AccelerateDecelerateInterpolator());
// start the animation
        set.start();
    }
}