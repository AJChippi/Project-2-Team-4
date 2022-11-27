package com.example.project_2_team_4;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity implements SensorEventListener {
        //  Views  //
    TextView txtLayout;
    TextView txtTimer;
        //  Variables  //
    //  Constants  //
    //  Arbitrary values that adds a threshold on when to start listening for sensor events
    int ACC_THRESHOLD = 1;
    int SENSOR_THRESHOLD = 2;
    int BUOYANCY_THRESHOLD = 3;
    String TAG = "MYTAG";
    String TAG2 = "MYTAG2";
    //Shared Pref values
    SettingValues settings;
    String tapAction;
    String previousText;
    String command;
    int count = 0;
    //Speeds
    float accStartingX;
    float accStartingY;
    float topSpeed = 0;
    //Flags
    boolean timerFinished = false;
    boolean flagStartingAcc = false;
    //Arraylists
    ArrayList<Float> arlValuesX;
    ArrayList<Float> arlValuesY;
    ArrayList<Float> arlTopSpeeds;
    ArrayList<Float> arlTempSpeeds;
    //Sensors
    SensorManager sensorManager;
    Sensor accSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Find IDs and set Settings
        settings = SettingValues.getInstance();
        txtLayout = findViewById(R.id.txtLayout);
        txtTimer = findViewById(R.id.txtTimer);

        //Initialize Arraylists
        arlValuesX = new ArrayList<>();
        arlValuesY = new ArrayList<>();
        arlTopSpeeds = new ArrayList<>();
        arlTempSpeeds = new ArrayList<>();

        //Initialize sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Get new command until maximum commands has been reached
        pickNewAction();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Once the timer has been completed, start listening for Accelerometer events
        if (timerFinished) {
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    //Add X & Y values into appropriate lists
                    arlValuesX.add(sensorEvent.values[0]);
                    arlValuesY.add(sensorEvent.values[1]);
                    //Get starting accelerometer x & y values
                    if (!flagStartingAcc) {
                        accStartingX = sensorEvent.values[0];
                        accStartingY = sensorEvent.values[1];
                        flagStartingAcc = true;
                        Log.d(TAG, "Beginning: X -> " + accStartingX + " | Y -> " + accStartingY);
                    }
                    //Get the current command and run the respective method for listening to either
                    //  up / down / straight
                    if (command.equalsIgnoreCase("up") || command.equalsIgnoreCase("down"))
                        checkPunchY(sensorEvent.values[0], sensorEvent.values[1]);
                    if (command.equalsIgnoreCase("straight"))
                        checkPunchX(sensorEvent.values[0], sensorEvent.values[1]);
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    //Determine whether or not the user is punching up or down
    public void checkPunchY(float accValueX, float accValueY) {
        //Command is UP. Check if the user is beginning to punch up
        if (arlValuesY.get(arlValuesY.size() - 1) > accStartingY + ACC_THRESHOLD) {
            //Find the top y-value
            if (command.equalsIgnoreCase("up")) {
                findTopSpeedY(accValueX, accValueY);
            }
        //Command is DOWN. Check if the user is beginning to punch down
        } else if (arlValuesY.get(arlValuesY.size() - 1) + ACC_THRESHOLD < accStartingY) {
            if (command.equalsIgnoreCase("down")) {
                findTopSpeedY(accValueX, accValueY);
            }
        } else {
            //not punching
            //clear the arraylist
            arlValuesY.clear();
        }
    }

    //Find and grab the top y-value from the accelerometer
    public void findTopSpeedY(float accValueX, float accValueY) {
        //Command is up
        //Accelerometer should be increasing. Get top speed and once the
        //  Y-value is smaller, stop listening, send data to ScoreBoardActivity and
        //  swap activities
        //Threshold is set in order to give leeway to getting values instead of grabbing every
        //  value
        if (command.equalsIgnoreCase("up") && accValueY > accStartingY + SENSOR_THRESHOLD) {
            if (topSpeed < accValueY && topSpeed >= 0) {
                topSpeed = accValueY;
                arlTempSpeeds.add(accValueX);
                //Log.d(TAG, "FASTER ----> " + topSpeed);
            } else if (topSpeed - ACC_THRESHOLD + 1 > accValueY && topSpeed >= 0) {
                //Temp list is utilized here to ensure that the user is NOT punching straight
                float tempSum = 0;
                for (int i = 0; i < arlTempSpeeds.size(); i++)
                    tempSum += arlTempSpeeds.get(i);

                //Temp list size is utilized here to ensure if it's greater than the threshold, that
                //  the user is NOT punching down
                if (tempSum < 0 && arlTempSpeeds.size() > BUOYANCY_THRESHOLD) {
                    //Punching up was successful and has ended.
                    // Find top speed and check if there is a new command
                        //Log.d(TAG, "SLOWER ----> " + accValueY);
                        //Log.d(TAG, "TOP SPEED: " + topSpeed + " | Start_Y: " + accStartingY);
                    topSpeed -= accStartingY;
                    arlTopSpeeds.add(topSpeed);
                    //Stop Listening and check for new commands
                    stopListening();
                    timerFinished = false;
                    resetValues();
                    vibrate();
                    count++;
                    pickNewAction();
                } else {
                    resetValues();
                }
            } else {
                resetValues();
            }
            //Command is down
        } else if (command.equalsIgnoreCase("down")) {
            //Speed value is going negative when punching down
            if (topSpeed - ACC_THRESHOLD > accValueY) {
                topSpeed = accValueY;
                arlTempSpeeds.add(topSpeed);
                //Log.d(TAG, "FASTER ----> " + topSpeed);
             //Y-value is larger and speed should be below 0
            } else if (topSpeed < accValueY && topSpeed < 0) {
                //Temp list size is here to ensure that the user is NOT punching up
                if (arlTempSpeeds.size() > BUOYANCY_THRESHOLD) {
                    //Punching down was successful and has ended.
                    // Find top speed and check if there is a new command
                        //Log.d(TAG, "SLOWER ----> " + accValueY);
                        //Log.d(TAG, "Temps: " + arlTempSpeeds.size());
                    //Convert to positive values
                    if(accStartingY < 0)
                        accStartingY += -1;
                    topSpeed *= -1;
                    topSpeed -= accStartingY;
                    //TopSpeed is checked here to ensure that the user is NOT punching straight
                    if (topSpeed > 0) {
                        //Stop listening and check for new command
                        arlTopSpeeds.add(topSpeed);
                        stopListening();
                        timerFinished = false;
                        arlTempSpeeds.clear();
                        vibrate();
                        count++;
                        pickNewAction();
                    }
                } else {
                    resetValues();
                }
            } else {
                resetValues();
            }
        }
    }

    //Grab and find the top X-value from the accelerometer
    public void findTopSpeedX(float accValueX, float accValueY) {
        //Command is straight
        //Accelerometer should be increasing. Get top speed and once the
        //  X-value is smaller, stop listening, send data to ScoreBoardActivity and
        //  swap activities
        //Threshold is set in order to give leeway to getting values instead of grabbing every
        //  value

        if (command.equalsIgnoreCase("straight")) {
            //Start getting the top x-values
            if (topSpeed < accValueX && topSpeed >= 0) {
                topSpeed = accValueX;
                arlTempSpeeds.add(accValueY);
                //Log.d(TAG2, "FASTER ----> " + topSpeed);
            //Begin slower speed process
            } else if (topSpeed > accValueX + ACC_THRESHOLD && topSpeed >= 0) {
                //TempSum is utilized here to ensure that the user is NOT punching up
                float tempSum = 0;
                for (int i = 0; i < arlTempSpeeds.size(); i++)
                    tempSum += arlTempSpeeds.get(i);

                //TempSpeed size is utilized here to ensure that the user is NOT punching down
                if (tempSum > 0 && arlTempSpeeds.size() > BUOYANCY_THRESHOLD) {
                    //Slows down. Get top speed
                        //Log.d(TAG2, "SLOWER ----> " + accValueX);
                        //Log.d(TAG2, "TOP SPEED: " + topSpeed + " | Start_X: " + accStartingX);
                    topSpeed -= accStartingX;
                    arlTopSpeeds.add(topSpeed);
                    //Stop Listening and check for new command
                    sensorManager.unregisterListener(this);
                    timerFinished = false;
                    resetValues();
                    vibrate();
                    count++;
                    pickNewAction();
                } else {
                    resetValues();
                }
            } else {
                resetValues();
            }
        }
    }

    //Check to ensure the current command is a straight punch
    public void checkPunchX(float accValueX, float accValueY) {
        //determine if the user is punching straight
        if (arlValuesX.get(arlValuesX.size() - 1) > accStartingX + ACC_THRESHOLD) {
            if (command.equalsIgnoreCase("straight")) {
                findTopSpeedX(accValueX, accValueY);
            }
        } else {
            //not punching
            //clear the arraylist
            arlValuesX.clear();
        }
    }

    //All commands have been completed. Find the top speed from the commands the user has done.
    //  Send the top speed to the ScoreBoardActivity
    public void sendData() {
        SharedPreferences sharedPref = getSharedPreferences("Prefs", MODE_PRIVATE);
        SharedPreferences.Editor sharedEdit = sharedPref.edit();

        //Find the top acceleration from the list of punches
        Log.d(TAG, "ALL TOP SPEEDS: " + arlTopSpeeds);
        float topScore = 0;
        for (int i = 0; i < arlTopSpeeds.size(); i++) {
            if (topScore < arlTopSpeeds.get(i))
                topScore = arlTopSpeeds.get(i);
        }

        sharedEdit.putString("AvgScore", topScore + "");
        sharedEdit.apply();
        arlTopSpeeds.clear();
    }

    //Start 3 second countdown
    public void startTimer() {
        Timer timer = new Timer();
        int[] arrColors = {Color.RED, Color.RED, Color.YELLOW, Color.GREEN};
        TimerTask t = new TimerTask() {
            //Initial countdown time
            int COUNTDOWN = 3;
            int i = 0;

            @Override
            public void run() {
                //Change countdown text
                runOnUiThread(() -> txtTimer.setText(COUNTDOWN + ""));
                COUNTDOWN--;
                //Change background color
                runOnUiThread(() -> txtLayout.setBackgroundColor(arrColors[i]));
                i++;
                //countdown is finished. Start tasks here
                if (COUNTDOWN == 0) {
                    vibrate();
                    timerFinished = true;
                    timer.cancel();
                }
            }
        };
        try {
            //Start timer
            timer.schedule(t, 1000L, 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Get the next command or finish the game
    public void pickNewAction() {
        //End game when the number of punches has reached the user set amount of punches
        if (count == settings.MaxPunches) {
            count = 0;
            //Send the top speed
            sendData();
            //Show dialog that the game has ended
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("All commands completed. Tap Next to View Results");
            builder.setCancelable(false);
            builder.setPositiveButton("NEXT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //go to main activity
                    Intent intent = new Intent(getApplicationContext(), ScoreBoardActivity.class);
                    startActivity(intent);
                }
            });
            builder.show();
            return;
        }
        //Continue game
        if (previousText != null) {
            txtLayout.setText(previousText);
        }
        // SHARED PREF FOR HAND GRIP //
        SharedPreferences sharedPref = getSharedPreferences("settingsPref", MODE_PRIVATE);
        onStart();
        previousText = txtLayout.getText().toString();
        int randomAction = (int) (Math.random() * settings.actions.size());
        String[] actions = settings.actions.toArray(new String[0]);
        tapAction = actions[randomAction];
        Log.d("sfasfa1", tapAction);
        if (tapAction.equals("checkUp")) {
            command = "up";
            txtLayout.setText("Punch Up");
        } else if (tapAction.equals("checkDown")) {
            command = "down";
            txtLayout.setText("Punch Down");
        } else if (tapAction.equals("checkStraight")) {
            command = "straight";
            txtLayout.setText("Punch Straight");
        }
    }

    //Set the vibration for the phone
    public void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(1000);
        }
    }

    //Stop listening to sensors and set the timer to inactive
    public void stopListening() {
        sensorManager.unregisterListener(this);
        timerFinished = false;
    }

    //Listen and register sensors
    public void startListening() {
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Reset the temp Arraylist and top speed. One of three scenarios cause this to occur:
    // 1) The user was unable to punch fast enough for the sensor to grab
    // 2) The user was punching in the wrong direction
    // 3) The punch was successful and the values need to be reset for the incoming command
    public void resetValues() {
        arlTempSpeeds.clear();
        topSpeed = 0;
    }


    @Override
    protected void onStart() {
        super.onStart();
        startListening();
        //When GameActivity is in focus, begin the process for the timer
        startTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Stop listening for sensors
        stopListening();
    }
}