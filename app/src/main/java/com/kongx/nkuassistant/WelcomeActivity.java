package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {


    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_welcome);
        SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME,0);
        Information.ifAutoLogin = settings.getBoolean("ifAutoLogin",false);
        CookieManager cookieManager = new CookieManager();
        Connect.initialize(cookieManager);
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
            try {
                HttpCookie cookie = new HttpCookie("JSESSIONID",settings.getString("JSESSIONID",""));
                cookie.setDomain("222.30.49.10");
                cookie.setPath("/");
                cookie.setVersion(0);
                cookieManager.getCookieStore().add(new URI(Information.webUrl+"/"), cookie);
                Log.e("APP",cookieManager.getCookieStore().get(new URI(Information.webUrl+"/xsxk/studiedAction.do")).toString());
            } catch (URISyntaxException e) {
                Log.e("APP","???");
                e.printStackTrace();
            }
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
