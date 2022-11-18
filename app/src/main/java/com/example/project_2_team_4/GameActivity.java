package com.example.project_2_team_4;

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
import android.os.Bundle;
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
    int numOfCommands;
    float accStartingX;
    float accStartingY;
    float gyroStartingX;
    float gyroStartingY;
    float topSpeed = 0;
    String[] arrCommands = {"Right", "Left", "Up", "Down"};
    String strHandGrip;
    String TAG = "MYTAG";
    boolean timerActive = false;
    boolean flagStarting = false;
    ArrayList<Float> arlValuesX;
    ArrayList<Float> arlValuesY;
    ArrayList<Float> arlTempValues;
    ArrayList<Float> arlTopSpeeds;

    //Sensors
    SensorManager sensorManager;
    Sensor accSensor;
    Sensor gyroSensor;

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
        strHandGrip = "Right";

        //Initialize Arraylists
        arlValuesX = new ArrayList<>();
        arlValuesY = new ArrayList<>();
        arlTempValues = new ArrayList<>();
        arlTopSpeeds = new ArrayList<>();

        //Initialize sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(timerActive){
            switch (sensorEvent.sensor.getType()){
                case Sensor.TYPE_ACCELEROMETER:
                    //Get starting accelerometer values
                    arlValuesX.add(sensorEvent.values[0]);
                    arlValuesY.add(sensorEvent.values[1]);
                    if(!flagStarting){
                        accStartingX = sensorEvent.values[0];
                        accStartingY = sensorEvent.values[1];
                        flagStarting = true;
                        Log.d(TAG, "Beginning: " + accStartingX + " | " + accStartingY);
                    }
                    checkPunchY();
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    public void checkPunchY(){
        if(arlValuesY.size() < 6){
            return;
        }

        //Loop through received Y-accelerometer values
        for(int i = 0; i < arlValuesY.size()-3; i++){
            //If the next receiving value is greater than the starting Y-value, then
            //  the punch is initiated
            if(accStartingY < arlValuesY.get(i+1)){
                Log.d(TAG, "Punch: " + arlValuesY.get(i));
                //Record the top speed from the recorded punches, else, add some temporary values
                if(topSpeed < arlValuesY.get(i))
                    topSpeed = arlValuesY.get(i);
                else
                    arlTempValues.add(arlValuesY.get(i));

                //Arraylist will never be larger than 6, so when reached max size, check
                //  temporary values against the top speed
                //If the top speed is larger than any of the temp values, the punch may have
                //  reached maximum speed and is complete. Grab values
                if(arlValuesY.size() > 5){
                    for(int j = 0; j < arlTempValues.size(); j++){
                        if(arlTempValues.get(j) < topSpeed){
                            topSpeed -= accStartingY;
                            arlTopSpeeds.add(topSpeed);
                            Log.d(TAG, "TOP SPEED: " + topSpeed);
                            arlTempValues.clear();
                            arlValuesY.clear();
                            topSpeed = 0;
                            sendData();
                            break;
                        }
                    }
                }
            }
        }
        arlValuesY.clear();
    }


    public void sendData(){
        SharedPreferences sharedPref = getSharedPreferences("Prefs", MODE_PRIVATE);
        SharedPreferences.Editor sharedEdit = sharedPref.edit();

        float avgSpeed = 0;
        for(int i = 0; i < arlTopSpeeds.size(); i++)
            avgSpeed += arlTopSpeeds.get(i);


        avgSpeed /= arlTopSpeeds.size();
        sharedEdit.putString("AvgScore", avgSpeed+"");
        sharedEdit.apply();
        arlTopSpeeds.clear();

        Intent intent = new Intent(this, ScoreBoardActivity.class);
        startActivity(intent);
    }


    //Start 3 second countdown
    public void startTimer(){
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
                if(COUNTDOWN == 0){
                    timerActive = true;
                    timer.cancel();
                }
            }
        };
        try{
                //Start timer
                timer.schedule(t, 1000L, 1000L);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_UI);
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