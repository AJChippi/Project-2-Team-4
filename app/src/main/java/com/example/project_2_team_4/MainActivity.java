package com.example.project_2_team_4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnSettings, btnNewGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSettings = findViewById(R.id.btnSettings);
        btnNewGame = findViewById(R.id.btnNewGame);


        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        btnNewGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);
            startActivity(intent);
        });

        SettingValues settings = SettingValues.getInstance();

        // shared preferences
        SharedPreferences preferences = getSharedPreferences("settingsPref", MODE_PRIVATE);
        settings.MaxPunches = preferences.getInt("punches", 10);
        settings.actions.clear();
        //checking the check boxes
        if (preferences.getBoolean("checkUp", true)) {
            settings.actions.add("checkUp");
        }
        if (preferences.getBoolean("checkDown", true)) {
            settings.actions.add("checkDown");
        }
        if (preferences.getBoolean("checkStraight", true)) {
            settings.actions.add("checkStraight");
        }
//        if (preferences.getBoolean("radioGroup", true)) {
//            settings.actions.add("radioGroup");
//        }
    }
}