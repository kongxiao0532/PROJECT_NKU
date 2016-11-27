package com.kongx.nkuassistant;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {


    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_welcome);
        System.setProperty("java.net.useSystemProxies", "true");
        new Timer().schedule(new TimerTask() {
                                 public void run() {
                                     Intent localIntent = new Intent(WelcomeActivity.this, IndexActivity.class);
                                     WelcomeActivity.this.startActivity(localIntent);
                                     WelcomeActivity.this.finish();
                                 }
                             }
                , 2500);
    }
}
