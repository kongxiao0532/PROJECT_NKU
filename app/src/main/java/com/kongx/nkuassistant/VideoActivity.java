package com.kongx.nkuassistant;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.net.URL;

public class VideoActivity extends AppCompatActivity {
    private static final long mBackPressThreshold = 3500;
    private Toast mPressBackToast;
    private long mLastBackPress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPressBackToast = Toast.makeText(getApplicationContext(), R.string.press_back_again_to_exit, Toast.LENGTH_SHORT);
        setContentView(R.layout.activity_video);
        VideoView video = (VideoView) findViewById(R.id.videoView);
        String videoUrl = getIntent().getStringExtra("url");
        Uri uri = Uri.parse(videoUrl);
        video.setVideoURI(uri);
        video.setMediaController(new MediaController(this));
        video.start();
    }
    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - mLastBackPress) > mBackPressThreshold) {
            mPressBackToast.show();
            mLastBackPress = currentTime;
        } else {
            mPressBackToast.cancel();
            finish();
        }

    }
}
