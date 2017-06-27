package com.kongx.nkuassistant;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.net.URL;

public class VideoActivity extends AppCompatActivity {
    private static final long mBackPressThreshold = 3500;
    private Toast mPressBackToast;
    private VideoView mVideoPlayer;
    private long mLastBackPress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPressBackToast = Toast.makeText(getApplicationContext(), R.string.press_back_again_to_exit, Toast.LENGTH_SHORT);
        setContentView(R.layout.activity_video);

        mVideoPlayer = (VideoView) findViewById(R.id.videoView);

        //显示缓冲Indicator
        final TextView videoMessage = (TextView) findViewById(R.id.video_buffer_text);
        videoMessage.setVisibility(View.VISIBLE);

        mVideoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoMessage.setVisibility(View.GONE);
            }
        });

        String videoUrl = getIntent().getStringExtra("url");
        Uri uri = Uri.parse(videoUrl);
        mVideoPlayer.setVideoURI(uri);
        mVideoPlayer.setMediaController(new MediaController(this));
        mVideoPlayer.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("VIDEO",mVideoPlayer.isPlaying()?"PLAYING":"NOTPLAYING");
        if(!mVideoPlayer.isPlaying()){
            mVideoPlayer.start();
        }
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
