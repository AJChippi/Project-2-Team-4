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
    //Views
    TextView txtLayout;
    TextView txtTimer;
    ImageView imgLeftGrip;
    ImageView imgRightGrip;

    SettingValues settings;
    String tapAction;
    String previousText;

    String command;

    //Variables
    //  Arbitrary value that adds a threshold when to start listening for sensor events
    int ACC_THRESHOLD = 2;
    float accStartingX;
    float accStartingY;
    float topSpeed = 0;
    String currentHandGrip = "";
    String[] rightAndLeft = {"Right", "Left"};
    String[] upAndDown = {"Up", "Down"};
    String TAG = "MYTAG";
    boolean timerActive = false;
    boolean flagStartingAcc = false;
    boolean flagStartingGeo = false;
    ArrayList<Float> arlValuesX;
    ArrayList<Float> arlValuesY;
    ArrayList<Float> arlTopSpeeds;
    ArrayList<Float> arlTempSpeeds;

    String strUpAndDown;

    //Sensors
    SensorManager sensorManager;
    Sensor accSensor;
    Sensor geoSensor;

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        settings = SettingValues.getInstance();

        txtLayout = findViewById(R.id.txtLayout);
        txtTimer = findViewById(R.id.txtTimer);

        //Image Resources
        imgLeftGrip = findViewById(R.id.imgLeftGrip);
        imgRightGrip = findViewById(R.id.imgRightGrip);
        imgLeftGrip.setImageResource(R.drawable.left_grip);
        imgRightGrip.setImageResource(R.drawable.left_grip);
        imgLeftGrip.setVisibility(View.INVISIBLE);
        imgRightGrip.setVisibility(View.INVISIBLE);

        //Initialize Arraylists
        arlValuesX = new ArrayList<>();
        arlValuesY = new ArrayList<>();
        arlTopSpeeds = new ArrayList<>();
        arlTempSpeeds = new ArrayList<>();

        //Initialize sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        geoSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        pickNewAction();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (timerActive) {
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    //Get starting accelerometer values
                    arlValuesX.add(sensorEvent.values[0]);
                    arlValuesY.add(sensorEvent.values[1]);
                    if (!flagStartingAcc) {
                        accStartingX = sensorEvent.values[0];
                        accStartingY = sensorEvent.values[1];
                        flagStartingAcc = true;
                        Log.d(TAG, "Beginning: " + accStartingX + " | " + accStartingY);
                    }

                 //   if(currentHandGrip.equalsIgnoreCase("right") || currentHandGrip.equalsIgnoreCase("left")){
                        //   if (sensorEvent.values[1] + ACC_THRESHOLD < accStartingY)
                    if(flagStartingGeo){
             //           Log.d(TAG, "ACCELEROMETER: " + sensorEvent.values[1]);
                        checkPunchY(sensorEvent.values[0], sensorEvent.values[1]);
                    }
                    break;

                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                    Log.d(TAG, "GEO: " + Arrays.toString(sensorEvent.values));
                    getHandOrientation(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    public void checkPunchY(float accValueX, float accValueY) {
        //determine if the user is punching up or down
        if (arlValuesY.get(arlValuesY.size() - 1) > accStartingY + ACC_THRESHOLD) {
            if (command.equalsIgnoreCase("up")) {
      //          Log.d(TAG, "X-COORDS: " + accValueX);
      //          Log.d(TAG, "Punching Up");
                findTopSpeedY(accValueX, accValueY, command);

            } else {
      //          Log.d(TAG, "Incorrect: Not UP");
                //           arlValuesY.clear();
            }
        } else if (arlValuesY.get(arlValuesY.size() - 1) + ACC_THRESHOLD < accStartingY) {
            if (command.equalsIgnoreCase("down")) {
      //          Log.d(TAG, "X-COORDS: " + accValueX);
      //          Log.d(TAG, "Punching down");
                findTopSpeedY(accValueX, accValueY, command);
            } else {
       //         Log.d(TAG, "Incorrect: Not DOWN");
                //         arlValuesY.clear();
            }
            //punching down
        } else {
            //not punching
      //      Log.d(TAG, "Not punching");
            //clear the arraylist
            arlValuesY.clear();
        }
    }


    public void findTopSpeedY(float accValueX, float accValueY, String currentCommand) {
        //Command is up
        //Accelerometer should be increasing. Get top speed and once the
        //  Y-value is smaller, stop listening, send data to ScoreBoardActivity and
        //  swap activities
        //Threshold is set in order to give leeway to getting values instead of grabbing every
        //  value
        if (command.equalsIgnoreCase("up")) {
            if (topSpeed < accValueY && topSpeed >= 0) {
                topSpeed = accValueY;
                arlTempSpeeds.add(accValueX);
   //             Log.d(TAG, "FASTER ----> " + topSpeed);
            } else if (topSpeed - ACC_THRESHOLD > accValueY && topSpeed >= 0) {
                float tempSum = 0;
                for(int i = 0; i < arlTempSpeeds.size(); i++){
                    tempSum+= arlTempSpeeds.get(i);
                }
                if(tempSum < 0){
                    //Punching up was successful. Find top s[eed and check if there is a new command
      //              Log.d(TAG, "SLOWER ----> " + accValueY);
       //             Log.d(TAG, "TOTAL X: " + tempSum);
                    topSpeed -= accStartingY;
                    arlTopSpeeds.add(topSpeed);
        //            Log.d(TAG, "TOP SPEED: " + topSpeed + " | Start_Y: " + accStartingY);
                    //Stop Listening
                    stopListening();
                    timerActive = false;
                    arlTempSpeeds.clear();
                    //Send speed data and swap activities
                    vibrate();
                    count++;
                    pickNewAction();

                } else {
                    arlTempSpeeds.clear();
                    topSpeed = 0;
                }
            }
            //Command is down
        } else if (command.equalsIgnoreCase("down")) {
   //         Log.d(TAG, "Checking: " + accValueY);
            //Speed value is going down when punching down
            if (topSpeed - ACC_THRESHOLD > accValueY) {
                topSpeed = accValueY;
                arlTempSpeeds.add(topSpeed);
     //           Log.d(TAG, "FASTER ----> " + topSpeed);
                //Y-value is larger and speed is below 0. Essentially in negatives
            } else if (topSpeed < accValueY && topSpeed < 0) {
                //Punching down was successful. Find top speed and check if there is a new command
                if (arlTempSpeeds.size() >= 4) {
       //             Log.d(TAG, "SLOWER ----> " + accValueY);
       //             Log.d(TAG, "Temps: " + arlTempSpeeds.size());
                    topSpeed *= -1;
                    topSpeed -= accStartingY;
                    arlTopSpeeds.add(topSpeed);
        //            Log.d(TAG, "TOP SPEED: " + topSpeed + " | Start_Y: " + accStartingY);
                    stopListening();
                    timerActive = false;
                    arlTempSpeeds.clear();
                    vibrate();
                    count++;
                    pickNewAction();
                } else {
                    arlTempSpeeds.clear();
                    topSpeed = 0;
                }
            }
        }
        //    arlValuesY.clear();
    }

    public void sendData() {
        SharedPreferences sharedPref = getSharedPreferences("Prefs", MODE_PRIVATE);
        SharedPreferences.Editor sharedEdit = sharedPref.edit();

        Log.d(TAG, "sendData: " + arlTopSpeeds);
        float topScore = 0;
        for (int i = 0; i < arlTopSpeeds.size(); i++){
            if(topScore < arlTopSpeeds.get(i))
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

    public void pickNewAction(){
        //End game when the number of punches has reached the user set amount of punches
        if(count == settings.MaxPunches){
            count = 0;
            //Send the top speed
            sendData();
            //Show dialog that the game has ended
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("All commands completed. Tap Next to View Results");
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
        if(previousText != null){
            txtLayout.setText(previousText);
        }
        // NEED SHARED PREF FOR HAND GRIP //
        setImgHandGrip("left");
      //  startTimer();
        onStart();
            previousText = txtLayout.getText().toString();
            int randomAction  = (int) (Math.random() * settings.actions.size());
            String[] actions = settings.actions.toArray(new String[0]);
            tapAction = actions[randomAction];

            if(tapAction == "checkUp"){
                command = "up";
                txtLayout.setText("Punch Up");
            }
            else if(tapAction == "checkDown"){
                command = "down";
                txtLayout.setText("Punch Down");
            }
            else if(tapAction == "checkStraight") {
                command = "straight";
                txtLayout.setText("Punch Straight");
            }
        }

    public void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(1000);
        }
    }

    public void stopListening(){
        sensorManager.unregisterListener(this);
        timerActive = false;
    }

    public void startListening(){
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, geoSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void setImgHandGrip(String strHandGrip){
        if (strHandGrip.equalsIgnoreCase("left")) {
            imgLeftGrip.setVisibility(View.VISIBLE);
        } else if (strHandGrip.equalsIgnoreCase("right")) {
            imgRightGrip.setVisibility(View.VISIBLE);
        }
    }

    public void getHandOrientation(float xValue, float yValue, float zValue){
        //Left-hand flat OR left-hand side
        // flat: [+, -, -]  side: [+, +, -]
        Log.d(TAG, "Checking: " + xValue + " | " + yValue + " | " + zValue);

        if((xValue > 0 && yValue < 0 && zValue < 0) ||
                (xValue > 0 && yValue > 0 && zValue < 0)){
            currentHandGrip = "left";
        }

        //Right-hand flat OR right-hand side
        // flat: [-, -, -]  side: [-, +, -]
        else if((xValue < 0 && yValue < 0 && zValue < 0) ||
                (xValue < 0 && yValue > 0 && zValue < 0) ||
                (xValue > 0 && yValue > 0 && zValue > 0)){
            currentHandGrip = "right";
        }
        else
            currentHandGrip = "";

        Log.d(TAG, "getHandOrientation: " + currentHandGrip);
        flagStartingGeo = currentHandGrip.equalsIgnoreCase("left");
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

    @Override
    protected void onResume() {
        super.onResume();
    }
}