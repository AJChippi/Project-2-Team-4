package com.example.project_2_team_4;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Scanner;

public class ScoreBoardActivity extends AppCompatActivity {
    ListView lstScores ;
    Button btnMenu ;
    Button btnTryAgain;
    Button btnClear;
    TextView txtAvgPunch ;
    TextView txtPlacement;
    ArrayList<ScoreHolder> scoreHolders;
    SharedPreferences sharedPref;
    SharedPreferences.Editor prefEditor;
    String userName="";
    String userScore="0";
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;
    boolean match = false;
    EditText etName;


    /* NAMED THE SHARED PREFS: "Prefs"
     * NEED 1 sharedPrefs from GAME ACTIVITY
     * 1. the avg punch speed passed as a string under the name "AvgScore"
     * */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);

        lstScores = findViewById(R.id.lstScores);
        btnMenu = findViewById(R.id.btnMenu);
        btnTryAgain = findViewById(R.id.btnTryAgain);
        txtAvgPunch =findViewById(R.id.txtAvgPunch);
        txtPlacement = findViewById(R.id.txtPlacement);
        sharedPref = getSharedPreferences("Prefs", MODE_PRIVATE);
        prefEditor = sharedPref.edit();
        scoreHolders = new ArrayList<>();
        txtPlacement.setVisibility(View.INVISIBLE);
        btnClear = findViewById(R.id.btnClear);
        ScoreHolderAdapter adapter = new ScoreHolderAdapter(scoreHolders);
        lstScores.setAdapter(adapter);

        txtAvgPunch.setText("Your Avg Punch Speed: "+sharedPref.getString("AvgScore","MISSING PREF") +" m/s²");




        btnTryAgain.setOnClickListener(v -> {
            finish();
        });

        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        fillArrayDummy();


        btnClear.setOnClickListener(view -> {
            scoreHolders.clear();
       clearMostPrefs();
       fillArrayDummy();
            String temp = txtAvgPunch.getText()+"";
            String temp1 = txtPlacement.getText()+"";
            userScore = "0";
            populateScoreBoard();
            txtAvgPunch.setText(temp);
            txtPlacement.setText(temp1);
            adapter.notifyDataSetChanged();

        });
            populateScoreBoard();
           userScore = sharedPref.getString("AvgScore","0");
            showPopUp();
    }


    //updates array to new table inputs
    public void updateStandings(){

        for(int i = 0;i<10;i++){
            String name = scoreHolders.get(i).name;
            String score = scoreHolders.get(i).score;

            prefEditor.putString(i+"Name", name);
            prefEditor.putString(i+"Score", score);
            prefEditor.commit();
        }
    }


    //puts all scores in the score board and puts the users in if it made it
    public boolean populateScoreBoard(){



        boolean matchFound = false;
        for(int i = 0;i<10;i++){
            String name;
            String score;
            if(matchFound){
                name = sharedPref.getString((i-1)+"Name","");
                score = sharedPref.getString((i-1)+"Score","0");
            }else{
                name =  sharedPref.getString(i+"Name","");
                score = sharedPref.getString(i+"Score","0");

            }

            ScoreHolder scoreHolder;
            if(!matchFound&&Double.parseDouble(userScore)>Double.parseDouble(score)){
                matchFound =true;
                scoreHolder = new ScoreHolder(userName,userScore,i+1+"");
                txtPlacement.setText("You placed: "+ordinalSuffix(i+1));
                txtPlacement.setVisibility(View.VISIBLE);

            }else{
                scoreHolder = new ScoreHolder(name,score,i+1+"");}



            scoreHolders.set(i,scoreHolder);

        }

        updateStandings();
        return  matchFound;
    }

    //putting suffix on placement
    public String ordinalSuffix(int i) {
        double j = i % 10;
        double k = i % 100;
        if (j == 1 && k != 11) {
            return i + "st";
        }
        if (j == 2 && k != 12) {
            return i + "nd";
        }
        if (j == 3 && k != 13) {
            return i + "rd";
        }
        return i + "th";
    }

    //Info for score holder, name, score, placement
    // and showing:  used for blank entries, to turn of on the board
    class ScoreHolder{

        String name;
        String score;
        boolean showing = false;
        String placement;

        public ScoreHolder(String name, String score,String placement) {
            this.name = name;
            this.score = score;
            this.placement = placement;
            if(Double.parseDouble(score)>0){
                showing=true;
            }
        }

        public ScoreHolder(){
            this.name="";
            this.score="0";
            this.showing=false;
        }

    }

    //adapts score_holder_layout to list view
    class ScoreHolderAdapter extends BaseAdapter {
        ArrayList<ScoreHolder> scoreHolders;


        public ScoreHolderAdapter(ArrayList<ScoreHolder> scoreHolders) {
            this.scoreHolders = scoreHolders;
        }




        @Override
        public int getCount() {
            return scoreHolders.size();
        }

        @Override
        public Object getItem(int i) {
            return scoreHolders.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null)
                view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.score_holder_layout,viewGroup,false);

            ScoreHolder s = (ScoreHolder) getItem(i);

            TextView txtName = view.findViewById(R.id.txtName);
            TextView txtScore = view.findViewById(R.id.txtScore);

            if(s.showing==false)
                txtScore.setVisibility(View.INVISIBLE);
            else{
                txtScore.setVisibility(View.VISIBLE);
            }

            txtName.setText(s.placement+". "+s.name);
            txtScore.setText(s.score+" m/s²");


            return view;
        }
    }

//makes pop up show up
    public void showPopUp(){
    dialogBuilder = new AlertDialog.Builder(this);
    View popUp = getLayoutInflater().inflate(R.layout.score_pop_up, null);
         etName = popUp.findViewById(R.id.txtName);
       Button btnSubmit = popUp.findViewById(R.id.btnSubmit);
       match = false;
       for (int i = 0;i<10;i++){
           if(Double.parseDouble(sharedPref.getString("AvgScore","0"))>Double.parseDouble(scoreHolders.get(i).score)){
               match = true;

           }
       }
       if(match){
           TextView txtYourScore = popUp.findViewById(R.id.txtYourScore);
           txtYourScore.setText("Your score made the score board!");
           TextView txtEnterName = popUp.findViewById(R.id.txtEnterName);
           txtEnterName.setVisibility(View.VISIBLE);
            etName = popUp.findViewById(R.id.etName);
           etName.setVisibility(View.VISIBLE);
           btnSubmit.setText("Submit");
       }else{
           TextView txtYourScore = popUp.findViewById(R.id.txtYourScore);
           txtYourScore.setText("Your score did not make the score board!");
           TextView txtEnterName = popUp.findViewById(R.id.txtEnterName);
           txtEnterName.setVisibility(View.INVISIBLE);
            etName = popUp.findViewById(R.id.etName);
           etName.setVisibility(View.INVISIBLE);
           btnSubmit.setText("OK");
       }




       btnSubmit.setOnClickListener(view -> {
           if(match)
               userName = etName.getText()+"";
           populateScoreBoard();
           dialog.dismiss();

       });
       dialogBuilder.setView(popUp);
       dialog = dialogBuilder.create();
       dialog.show();

    }


    //fills array with dummy values if empty
    public void fillArrayDummy(){
        if(scoreHolders.isEmpty()){
            for (int i = 0;i<10;i++){
                scoreHolders.add(i,new ScoreHolder());

            }
        }
    }


    public void clearMostPrefs(){
        for(int i = 0;i<10;i++){

            prefEditor.putString(i+"Name", "");
            prefEditor.putString(i+"Score","0");
            prefEditor.commit();
        }
    }

}