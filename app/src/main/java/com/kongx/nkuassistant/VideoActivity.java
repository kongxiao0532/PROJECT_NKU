package com.kongx.nkuassistant;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import java.net.URL;

public class VideoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        VideoView video = (VideoView) findViewById(R.id.videoView);
        String videoUrl = getIntent().getStringExtra("url");
        Uri uri = Uri.parse(videoUrl);
        video.setVideoURI(uri);
        video.setMediaController(new MediaController(this));
        video.start();
    }
}
