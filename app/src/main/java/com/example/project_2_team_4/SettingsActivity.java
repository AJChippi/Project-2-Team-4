package com.example.project_2_team_4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    TextView txtSeek;
    RadioGroup radioGroup;
    CheckBox checkUp, checkDown, checkStraight;
    SeekBar seekBar;
    String selectedHand;
    Boolean booleanRight;
    Button btnSave, btnBack2Main;
    SharedPreferences sharedPref;
    SettingValues settingValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        txtSeek = findViewById(R.id.txtSeek);
        radioGroup = findViewById(R.id.radioGroup);
        btnSave = findViewById(R.id.btnSave);
        btnBack2Main = findViewById(R.id.btnBack2Menu);
        seekBar = findViewById(R.id.seekBar);
        checkUp = findViewById(R.id.checkUp);
        checkDown = findViewById(R.id.checkDown);
        checkStraight = findViewById(R.id.checkStraight);

        sharedPref = getSharedPreferences("settingsPref", MODE_PRIVATE);
        checkUp.setChecked(sharedPref.getBoolean("checkUp", true));
        checkDown.setChecked(sharedPref.getBoolean("checkDown", true));
        checkStraight.setChecked(sharedPref.getBoolean("checkStraight", true));
        seekBar.setProgress(sharedPref.getInt("punches", 10));
        //radioGroup.check(sharedPref.getBoolean("radioGroup", true) ? R.id.radioButton : R.id.radioButton2);

        txtSeek.setText(String.valueOf(seekBar.getProgress()));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtSeek.setText(String.valueOf(i));
                settingValues.MaxPunches = seekBar.getProgress();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            RadioButton rdoChecked = findViewById(i);
            selectedHand = String.valueOf(rdoChecked.getText());
            if (selectedHand.equals("Right")) {
                booleanRight = true;
            } else if (selectedHand.equals("Left")) {
                booleanRight = true;
            }else{
                booleanRight = false;
            }
        });

        btnSave.setOnClickListener(v -> {
            sharedPref = getSharedPreferences("settingsPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            //editor.putBoolean("radioGroup", booleanRight);
            editor.putInt("punches", seekBar.getProgress());
            editor.putBoolean("checkUp", checkUp.isChecked());
            editor.putBoolean("checkDown", checkDown.isChecked());
            editor.putBoolean("checkStraight", checkStraight.isChecked());
            editor.commit();

            Toast.makeText(this, settingValues.actions.size()+"", Toast.LENGTH_SHORT).show();

        });

        btnBack2Main.setOnClickListener(v -> {
            finish();
        });

    }


}