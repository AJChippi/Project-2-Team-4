package com.example.project_2_team_4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    TextView txtSeek;
    CheckBox checkUp, checkDown, checkStraight;
    SeekBar seekBar;
    String selectedHand;
    Boolean booleanRight;
    Button btnSave, btnBack2Main;
    SharedPreferences sharedPref;
    SettingValues settingValues;
    String Orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        txtSeek = findViewById(R.id.txtSeek);
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
        Orientation = sharedPref.getString("Orientation", "Right");
        booleanRight = sharedPref.getBoolean("radioGroup", true);
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

        btnSave.setOnClickListener(v -> {
            if(checkSelectionExists()==true){
                sharedPref = getSharedPreferences("settingsPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("Orientation", Orientation);
                switch (Orientation) {
                    case "Left":
                        booleanRight = false;
                        break;
                    case "Right":
                        booleanRight = true;
                        break;
                }
                editor.putBoolean("radioGroup", booleanRight);
                editor.putInt("punches", seekBar.getProgress());
                editor.putBoolean("checkUp", checkUp.isChecked());
                editor.putBoolean("checkDown", checkDown.isChecked());
                editor.putBoolean("checkStraight", checkStraight.isChecked());
                editor.commit();

                Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();;
            } else if(checkSelectionExists()==false){
                Toast.makeText(this, "Please select at least one direction", Toast.LENGTH_SHORT).show();
            }




        });

        btnBack2Main.setOnClickListener(v -> {
            finish();
        });

    }
    public void checkDirection(View view){
        CheckBox chkSelected = (CheckBox) view;
        String addToActionList = null;
        if(chkSelected.getText().equals("Punch Up")){
            addToActionList= "checkUp";
        } else if(chkSelected.getText().equals("Punch Down")){
            addToActionList= "checkDown";
        } else if(chkSelected.getText().equals("Punch Straight")){
            addToActionList= "checkStraight";
        }


        if(chkSelected.isChecked()) {
            settingValues.actions.add(addToActionList);
        }
        else {
            settingValues.actions.remove(addToActionList);
        }
        Log.d("asfdasfasf", String.valueOf(settingValues.actions));
    }

    private boolean checkSelectionExists() {
        boolean selectionExists = false;
        selectionExists = (boolean) ((CheckBox) findViewById(R.id.checkUp)).isChecked() ? true : false ||
                (boolean) ((CheckBox) findViewById(R.id.checkDown)).isChecked() ? true : false ||
                (boolean) ((CheckBox) findViewById(R.id.checkStraight)).isChecked() ? true : false;
        return selectionExists;
    }
}