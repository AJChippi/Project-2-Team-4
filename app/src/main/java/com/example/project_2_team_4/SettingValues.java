package com.example.project_2_team_4;

import android.util.Log;

import java.util.ArrayList;

public class SettingValues {
    private static SettingValues instance = null;
    public static ArrayList<String> actions = new ArrayList<String>() {{
        add("Punch Up");
        add("Punch Down");
        add("Punch Straight");
        add("punches");
        //add("radioGroup");

    }};

    public static int MaxPunches = 10;

    // Create private constructor
    private SettingValues() {
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */

// Create public method to get instance of class
    public static SettingValues getInstance() {
        if (instance == null) {
            instance = new SettingValues();
        }
        return instance;
    }
}


