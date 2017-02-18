package com.kongx.nkuassistant;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.icu.text.IDNA;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tk.sunrisefox.httprequest.*;

public class FeedbackFragment extends Fragment implements View.OnClickListener, Connect.Callback{
    private String version;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = (View) inflater.inflate(R.layout.fragment_feedback, container, false);
        Button mCheckForUpdate = (Button) myView.findViewById(R.id.button_check_for_update);
        Button mFeedback = (Button) myView.findViewById(R.id.button_feedback);
        TextView mVersion = (TextView) myView.findViewById(R.id.version);
        mCheckForUpdate.setOnClickListener(FeedbackFragment.this);
        mFeedback.setOnClickListener(FeedbackFragment.this);
        mVersion.setText(Information.version);
        return myView;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.button_check_for_update){
            new Request.Builder().url(Information.UPDATE_URL).build().send(this);

        }else if(view.getId() == R.id.button_feedback){
            Intent seedEmail=new Intent(Intent.ACTION_SENDTO);
            seedEmail.setData(Uri.parse(Information.FEEDBACK_EMAIL));
            seedEmail.putExtra(Intent.EXTRA_SUBJECT, Information.FEEDBACK_SUBJECT);
            startActivity(seedEmail);
        }
    }

    @Override
    public void onNetworkComplete(Response response) {
        if(response.code() != 200) ; //TODO: Why not 200?
        String retString = response.body();
        Pattern pattern;
        Matcher matcher;
        String versionNew = "";
        String apkSize = "";
        String updateTime = "";
        String updateLog = "";
        pattern = Pattern.compile("<version>(.+)(</version>)");
        matcher = pattern.matcher(retString);
        if(matcher.find())  versionNew = matcher.group(1);
        pattern = Pattern.compile("<size>(.+)(</size>)");
        matcher = pattern.matcher(retString);
        if(matcher.find())  apkSize = matcher.group(1);
        pattern = Pattern.compile("<updateTime>(.+)(</updateTime>)");
        matcher = pattern.matcher(retString);
        if(matcher.find())  updateTime = matcher.group(1);
        pattern = Pattern.compile("<updateLog>(.+)(</updateLog>)");
        matcher = pattern.matcher(retString);
        if(matcher.find())  updateLog = matcher.group(1);
        pattern = Pattern.compile("<downloadLink>(.+)(</downloadLink>)");
        matcher = pattern.matcher(retString);
        matcher.find();
        final String downloadLink = matcher.group(1);
        if (Information.version.equals(versionNew)){
            Toast.makeText(getActivity(), "暂无可用更新", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.update_available))
                    .setMessage("更新包大小："+apkSize+"\n更新时间："+updateTime+"\n更新内容："+updateLog)
                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = Uri.parse(downloadLink);
                            Intent intent =new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                            Toast.makeText(getActivity(), "更新开始下载...", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        }
        if(!version.equals(retString)){
            Toast.makeText(getActivity(), "有版本更新", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNetworkError(Exception exception) {

    }
}
