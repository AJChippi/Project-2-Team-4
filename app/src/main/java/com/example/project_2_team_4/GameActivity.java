package com.example.project_2_team_4;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    //Views
    TextView txtLayout;
    TextView txtTimer;
    ImageView imgLeftGrip;
    ImageView imgRightGrip;
    Button btnPause;

    //Variables
    int ACC_THRESHOLD = 1;
    int numOfCommands;
    float accStartingX;
    float accStartingY;
    float gyroStartingX;
    float gyroStartingY;
    float topSpeed = 0;
    String[] rightAndLeft = {"Right", "Left"};
    String[] upAndDown = {"Up", "Down"};
    String strHandGrip;
    String TAG = "MYTAG";
    boolean timerActive = false;
    boolean flagStarting = false;
    ArrayList<Float> arlValuesX;
    ArrayList<Float> arlValuesY;
    ArrayList<Float> arlTempValues;
    ArrayList<Float> arlTopSpeeds;

    ArrayList<String> arlCommands;
    String strUpAndDown;

    //Sensors
    SensorManager sensorManager;
    Sensor accSensor;
    Sensor gyroSensor;
    AlertDialog.Builder alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        txtLayout = findViewById(R.id.txtLayout);
        txtTimer = findViewById(R.id.txtTimer);
        imgLeftGrip = findViewById(R.id.imgLeftGrip);
        imgRightGrip = findViewById(R.id.imgRightGrip);
        btnPause = findViewById(R.id.btnPause);

        //Get Settings Preferences
        //For now, set things to default
        numOfCommands = 2;
        strHandGrip = "Right HAND";

        //Initialize Arraylists
        arlValuesX = new ArrayList<>();
        arlValuesY = new ArrayList<>();
        arlTempValues = new ArrayList<>();
        arlTopSpeeds = new ArrayList<>();
        arlCommands = new ArrayList<>();

        //Initialize sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        alertDialog = new AlertDialog.Builder(this);

        for (int i = 0; i < numOfCommands; i++) {
            int upAndDownRandom = (int) (Math.random() * 2);
            // get the random value of the array
            arlCommands.add(upAndDown[upAndDownRandom]);
        }

        // set the text of the layout
        //    txtLayout.setText(arlCommands.get(0));
        txtLayout.setText(arlCommands.get(0));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (timerActive) {
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    //Get starting accelerometer values
                    arlValuesX.add(sensorEvent.values[0]);
                    arlValuesY.add(sensorEvent.values[1]);
                    if (!flagStarting) {
                        accStartingX = sensorEvent.values[0];
                        accStartingY = sensorEvent.values[1];
                        flagStarting = true;
                        Log.d(TAG, "Beginning: " + accStartingX + " | " + accStartingY);
                    }

                    if (sensorEvent.values[1] + ACC_THRESHOLD < accStartingY)
                        Log.d(TAG, "onSensorChanged: " + sensorEvent.values[1]);

                    checkPunchY(sensorEvent.values[1]);
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void checkPunchY(float accValueY) {
        //determine if the user is punching up or down
        if (arlValuesY.get(arlValuesY.size() - 1) > accStartingY + ACC_THRESHOLD) {
        // <--- NO THRESHOLD FOR EMULATOR ---> //
        //if (arlValuesY.get(arlValuesY.size() - 1) > accStartingY) {
            if (arlCommands.get(0).equalsIgnoreCase("up")) {
                findTopSpeedY(accValueY, arlCommands.get(0));
            } else {
                Log.d(TAG, "Incorrect: Not UP");
            }
        } else if (arlValuesY.get(arlValuesY.size() - 1) + ACC_THRESHOLD < accStartingY) {
            // <--- NO THRESHOLD FOR EMULATOR ---> //
       // } else if (arlValuesY.get(arlValuesY.size() - 1) < accStartingY) {
            if (arlCommands.get(0).equalsIgnoreCase("down")) {
                Log.d(TAG, "Punching down");
                findTopSpeedY(accValueY, arlCommands.get(0));
            } else {
                Log.d(TAG, "Incorrect: Not DOWN");
            }
            //punching down
        } else {
            //not punching
            Log.d(TAG, "Not punching");
            //clear the arraylist
            arlValuesY.clear();
        }
    }


    public void findTopSpeedY(float accValueY, String currentCommand) {
        //Command is up
        //Accelerometer should be increasing. Get top speed and once the
        //  Y-value is smaller, stop listening, send data to ScoreBoardActivity and
        //  swap activities
        //Threshold is set in order to give leeway to getting values instead of grabbing every
        //  value
        if (currentCommand.equalsIgnoreCase("up")) {
            if (topSpeed < accValueY) {
                topSpeed = accValueY;
            } else if (topSpeed - ACC_THRESHOLD > accValueY) {
            // <--- NO THRESHOLD FOR EMULATOR ---> //
        //    } else if (topSpeed > accValueY) {
                //Slows down. Get top speed
                topSpeed -= accStartingY;
                arlTopSpeeds.add(topSpeed);
                Log.d(TAG, "TOP SPEED: " + topSpeed + " | Start_Y: " + accStartingY);
                //Stop Listening
                sensorManager.unregisterListener(this);
                timerActive = false;
                //Send speed data and swap activities
                sendData();
                Intent intent = new Intent(getApplicationContext(), ScoreBoardActivity.class);
                startActivity(intent);
            }
            //Command is down
        } else if (currentCommand.equalsIgnoreCase("down")) {
            Log.d(TAG, "Checking: " + accValueY);
            //Speed value is going down when punching down
            if (topSpeed - ACC_THRESHOLD > accValueY) {
            // <--- NO THRESHOLD FOR EMULATOR ---> //
         //   if (topSpeed > accValueY) {
                topSpeed = accValueY;
                Log.d(TAG, "FASTER ----> " + topSpeed);
                //Y-value is larger and speed is below 0. Essentially in negatives
            } else if (topSpeed < accValueY && topSpeed < 0) {
                Log.d(TAG, "SLOWER ----> " + accValueY);
                topSpeed *= -1;
                topSpeed -= accStartingY;
                arlTopSpeeds.add(topSpeed);
                Log.d(TAG, "TOP SPEED: " + topSpeed + " | Start_Y: " + accStartingY);
                sensorManager.unregisterListener(this);
                timerActive = false;
                sendData();
                Intent intent = new Intent(getApplicationContext(), ScoreBoardActivity.class);
                startActivity(intent);
            }
        }
        arlValuesY.clear();
    }

    public void sendData() {
        SharedPreferences sharedPref = getSharedPreferences("Prefs", MODE_PRIVATE);
        SharedPreferences.Editor sharedEdit = sharedPref.edit();

        Log.d(TAG, "sendData: " + arlTopSpeeds);
        float avgSpeed = 0;
        for (int i = 0; i < arlTopSpeeds.size(); i++)
            avgSpeed += arlTopSpeeds.get(i);

        avgSpeed /= arlTopSpeeds.size();
        sharedEdit.putString("AvgScore", avgSpeed + "");
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
                    timerActive = true;
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

    public void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(1000);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //When GameActivity is in focus, begin the process for the timer
        startTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Stop listening for sensors
        sensorManager.unregisterListener(this);
        timerActive = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}