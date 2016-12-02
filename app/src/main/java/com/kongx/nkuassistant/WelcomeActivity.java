package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {


    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_welcome);
        SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME,0);
        Information.ifFirstStart = settings.getBoolean("ifFirstStart",false);
        Information.studiedCourseCount = Integer.parseInt(settings.getString("studiedCourseCount","0"));
        CookieManager cookieManager = new CookieManager();
        Connect.initialize(cookieManager);
        if(!Information.ifFirstStart){
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
        settings = getSharedPreferences(Information.COURSE_PREFS_NAME,0);
        Information.selectedCourseCount = Integer.parseInt(settings.getString("selectedCourseCount","0"));
        if(Information.selectedCourseCount != 0){
            HashMap<String,String> map;
            for(int i = 0;i < Information.selectedCourseCount;i++){
                map = new HashMap<>();
                map.put("index",settings.getString("index"+i,null));
                map.put("name",settings.getString("name"+i,null));
                map.put("dayOfWeek",settings.getString("dayOfWeek"+i,null));
                map.put("startTime",settings.getString("startTime"+i,null));
                map.put("endTime",settings.getString("endTime"+i,null));
                map.put("classRoom",settings.getString("classRoom"+i,null));
                map.put("classType",settings.getString("classType"+i,null));
                map.put("teacherName",settings.getString("teacherName"+i,null));
                map.put("startWeek",settings.getString("startWeek"+i,null));
                map.put("endWeek",settings.getString("endWeek"+i,null));
                Information.selectedCourses.add(map);
            }
        }
        System.setProperty("java.net.useSystemProxies", "true");
        new Timer().schedule(new TimerTask() {
                                 public void run() {
                                     Intent localIntent;
                                     if(Information.ifFirstStart)    {
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
