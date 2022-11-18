package com.example.project_2_team_4;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Date;


public class XYZAccelerometer extends AppCompatActivity implements SensorEventListener {
    double calibration = Double.NaN;
    SensorManager sensorManager;
    private boolean color = false;
    private TextView view;
    TextView textView4;
    private long lastUpdate;
    Sensor accelerationSensor;

    float appliedAcceleration = 0;
    float currentAcceleration = 0;
    float velocity = 0;
    Date lastUpdatedate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        view = findViewById(R.id.speed);
        textView4 = findViewById(R.id.textView4);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }
//        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);


//        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
//        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        lastUpdatedate = new Date(System.currentTimeMillis());
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }

        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        double a = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        if (calibration == Double.NaN)
            calibration = a;
        else {
            updateVelocity();
            currentAcceleration = (float)a;
        }
        Log.d("testtest", String.valueOf(currentAcceleration));

    }


    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = event.timestamp;
        if (accelationSquareRoot >= 2) //
        {
            if (actualTime - lastUpdate < 200) {
                return;
            }
            lastUpdate = actualTime;
            Toast.makeText(this, "Punch occured", Toast.LENGTH_SHORT).show();
            view.setText("SPEEDDDDD=== "+accelationSquareRoot);
            Log.i("SensorTestActivity","SPEEDDDDD1=== "+accelationSquareRoot+"     ");
        }
    }

    private void updateVelocity() {
        // Calculate how long this acceleration has been applied.
        Date timeNow = new Date(System.currentTimeMillis());
        long timeDelta = timeNow.getTime()-lastUpdatedate.getTime();
        lastUpdatedate.setTime(timeNow.getTime());

        // Calculate the change in velocity at the
        // current acceleration since the last update.
        float deltaVelocity = appliedAcceleration * (timeDelta/1000);
        appliedAcceleration = currentAcceleration;

        // Add the velocity change to the current velocity.
        velocity += deltaVelocity;

        final double mph = (Math.round(100*velocity / 1.6 * 3.6))/100;

        Log.i("SensorTestActivity","SPEEDDDDD2=== "+mph+"     "+velocity);
        textView4.setText(mph+"     "+velocity);
//        sensorManager.unregisterListener(this);

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

     }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        // register this class as a listener for the orientation and
//        // accelerometer sensors
//        sensorManager.registerListener(this,
//                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                SensorManager.SENSOR_DELAY_NORMAL);
//    }
//
    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
    }

}