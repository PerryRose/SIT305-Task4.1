package com.example.timer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Handler handler = new Handler();
    Runnable timerRunnable;
    TextView timerTextView, previousTimeTextView;
    EditText workoutTypeEditText;
    SharedPreferences sharedPreferences;

    long timeElapsedInSeconds = 0;
    private boolean isRunning;
    String TIME_ELAPSED = "TIME_ELAPSED",
            IS_RUNNING = "IS_RUNNING",
            WORKOUT_TYPE = "WORKOUT_TYPE";
    String workoutType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("Preferences", MODE_PRIVATE);

        // Set Up
        setUpWidgets();
        setUpTimer();

        loadWorkout();

        if (savedInstanceState != null) {
            // Load time elapsed
            timeElapsedInSeconds = savedInstanceState.getLong(TIME_ELAPSED);

            // Update timer text view
            updateTimerTextView();

            // Check if timer was running
            isRunning = savedInstanceState.getBoolean(IS_RUNNING);

            // Remember workout type
            workoutType = savedInstanceState.getString(WORKOUT_TYPE);

            // If it was running
            if (isRunning) {
                // Keep running
                handler.postDelayed(timerRunnable, 0);
            }
        }
    }

    private void saveWorkout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WORKOUT_TYPE, workoutType);
        editor.putLong(TIME_ELAPSED, timeElapsedInSeconds);
        editor.commit();
    }

    private void loadWorkout() {
        // Get workout
        String previousWorkout = sharedPreferences.getString(WORKOUT_TYPE, "");

        // If workout isn't default value
        if (!previousWorkout.isEmpty()) {
            // Turn Time Elapsed into formatted time
            long seconds = sharedPreferences.getLong(TIME_ELAPSED, 0);
            long minutes = (seconds % 3600) / 60;
            long hours = seconds / 3600;

            // Format new time
            String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

            // Update previous time label
            previousTimeTextView.setText("You spent " + time + " on " + previousWorkout + " last time.");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save values
        outState.putLong(TIME_ELAPSED, timeElapsedInSeconds);
        outState.putBoolean(IS_RUNNING, isRunning);
        outState.putString(WORKOUT_TYPE, workoutType);

    }

    private void setUpWidgets() {
        timerTextView = findViewById(R.id.timerTextView);
        workoutTypeEditText = findViewById(R.id.workoutTypeEditText);
        previousTimeTextView = findViewById(R.id.previousRecordedTimeTextView);
    }

    private void setUpTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                // Update text view
                updateTimerTextView();

                // Increment time
                timeElapsedInSeconds++;

                // Run again in one second
                handler.postDelayed(timerRunnable,1000);
            }
        };
    }

    public void updateTimerTextView() {
        // Update values
        long seconds = timeElapsedInSeconds;
        long minutes = (seconds % 3600) / 60;
        long hours = seconds / 3600;

        // Format new time
        String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

        // Update text view
        timerTextView.setText(time);
    }

    public void startTimer(View view) {
        // If Timer isn't already running
        if (!isRunning) {
            // If there is a workout type
            if (!workoutTypeEditText.getText().toString().trim().isEmpty()) {
                // Prevent timer from running simultaneously
                isRunning = true;

                // Get Workout type
                workoutType = workoutTypeEditText.getText().toString();

                // Don't allow value to be changed once timer has started
                workoutTypeEditText.setEnabled(false);

                // Start timer
                handler.postDelayed(timerRunnable, 0);
            }
            else {
                Toast.makeText(this, "Please enter a workout type.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void pauseTimer(View view) {
        if (isRunning) {
            // Set to false so startTimer can work
            isRunning = false;

            // Stop timer
            handler.removeCallbacks(timerRunnable);
        }
    }

    public void stopTimer(View view) {
        if (isRunning) {
            // Set to false so startTimer can work
            isRunning = false;

            // Stop timer
            handler.removeCallbacks(timerRunnable);

            // Save workout
            saveWorkout();

            // Load workout to update text
            loadWorkout();

            // Reset
            timeElapsedInSeconds = 0;
            timerTextView.setText("00:00:00");
            workoutTypeEditText.setText("");
            workoutTypeEditText.setEnabled(true);
        }
    }
}