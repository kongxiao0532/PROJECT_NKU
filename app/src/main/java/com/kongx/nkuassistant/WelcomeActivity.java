package com.kongx.nkuassistant;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity
{

    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_welcome);
//        GridLayout gridLayout = (GridLayout)findViewById(R.id.layout_welcome);
//        gridLayout.setPadding(0, getStatusBarHeight(), 0, 0);
        new Timer().schedule(new TimerTask()
        {
            public void run()
            {
                Intent localIntent = new Intent(WelcomeActivity.this, IndexActivity.class);
//                WelcomeActivity.this.startActivity(localIntent);
//                WelcomeActivity.this.finish();
            }
        }
                ,2500);
    }
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
