package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {


    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_welcome);
        SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME,0);
        Information.ifAutoLogin = settings.getBoolean("ifAutoLogin",false);
        if(!Information.ifAutoLogin){
            Information.name = "Name";
            Information.facultyName = "Faculty";
            Information.majorName = "Major";
            Information.id = "ID";
        }
        else {
            Information.name = settings.getString("StudentName","Name");
            Information.facultyName = settings.getString("FacultyName","Faculty");
            Information.id = settings.getString("StudentID","id");
            Information.majorName = settings.getString("MajorName","Major");
        }
        System.setProperty("java.net.useSystemProxies", "true");
        new Timer().schedule(new TimerTask() {
                                 public void run() {
                                     Intent localIntent;
                                     if(Information.ifAutoLogin)    {
                                         localIntent = new Intent(WelcomeActivity.this, IndexActivity.class);
                                     }
                                     else   {
                                         localIntent = new Intent(WelcomeActivity.this, EduLoginActivity.class);
                                     }
                                     WelcomeActivity.this.startActivity(localIntent);
                                     WelcomeActivity.this.finish();
                                 }
                             }
                , 2500);
    }
}
