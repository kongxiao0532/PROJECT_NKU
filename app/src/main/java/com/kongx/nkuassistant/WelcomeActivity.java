package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.util.ArrayList;

import cn.jpush.android.api.JPushInterface;

import static com.kongx.nkuassistant.Information.*;

public class WelcomeActivity extends AppCompatActivity {

    private boolean componentReady = false;
    private Intent startIntent = null;
    final TimerTask timerTask = new TimerTask() {
        public void run() {
            synchronized (this){
                while (!componentReady) try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            WelcomeActivity.this.startActivity(startIntent);
            WelcomeActivity.this.finish();
        }
    };

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        View v = View.inflate(getApplicationContext(),R.layout.activity_welcome,null);
        setContentView(v);
        new Timer().schedule(timerTask, 2000);


        ImageView startImg1 = (ImageView)v.findViewById(R.id.startImg);
        ImageView startImg2 = (ImageView)v.findViewById(R.id.startImg2);
        startImg1.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.splash_fade_in));
        startImg2.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.splash_move_in));

        JPushInterface.setDebugMode(true);
        JPushInterface.init(getApplication());

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        System.setProperty("java.net.useSystemProxies", "true");
        CookieManager cookieManager = new CookieManager();
        File bugCheckFile = new File(getExternalCacheDir(),new Date().getTime()+".txt");
        try { bugCheckFile.createNewFile(); } catch (IOException e) { }
        Connect.initialize(cookieManager);
        Connect.initializeBugCheck(bugCheckFile);
        SharedPreferences.Editor editor = settings.edit();
        String lastBugCheckFile = settings.getString("bugCheckFile",null);
        editor.putString("lastBugCheckFile",lastBugCheckFile == null ? bugCheckFile.getAbsolutePath() : lastBugCheckFile);
        editor.putString("bugCheckFile",bugCheckFile.getAbsolutePath());
        editor.apply();

        Connect.writeToBugCheck("Initialized with file "+bugCheckFile.getName());

        ifRemPass = settings.getBoolean(Strings.setting_remember_pwd, false);
        studiedCourseCount = settings.getInt(Strings.setting_studied_course_count, -1);
        name = settings.getString(Strings.setting_student_name, "Name");
        facultyName = settings.getString(Strings.setting_student_faculty, "Faculty");
        id = settings.getString(Strings.setting_studentID, "ID");
        majorName = settings.getString(Strings.setting_student_major, "Major");
        if(ifRemPass) {
            startIntent = new Intent(WelcomeActivity.this, IndexActivity.class);
            try {
                HttpCookie cookie = new HttpCookie("JSESSIONID", settings.getString("JSESSIONID", ""));
                cookie.setDomain("222.30.49.10");
                cookie.setPath("/");
                cookie.setVersion(0);
                cookieManager.getCookieStore().add(new URI(WEB_URL + "/"), cookie);
            } catch (URISyntaxException e) {
                Log.e("WelcomeActivity", "Caught URISyntaxException");
            }
        }else startIntent = new Intent(WelcomeActivity.this, EduLoginActivity.class);

        //get Curriculum Preferences
        settings = getSharedPreferences(COURSE_PREFS_NAME,0);
        selectedCourseCount = settings.getInt(Strings.setting_selected_course_count, -1);
        curriculum_lastUpdate = settings.getString(Strings.setting_last_update_time, null);
        if(selectedCourseCount != -1){
            HashMap<String,String> map;
            for(int i = 0;i < selectedCourseCount;i++){
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
                selectedCourses.add(map);
            }
        }

        //get Exams Preferences
        settings = getSharedPreferences(EXAM_PREFS_NAME,0);
        examCount = settings.getInt(Strings.setting_exam_count, -1);
        if(examCount != -1){
            HashMap<String,String> map;
            for(int i = 0;i < examCount;i++){
                map = new HashMap<>();
                map.put("name",settings.getString("name"+i,null));
                map.put("startTime",settings.getString("startTime"+i,null));
                map.put("endTime",settings.getString("endTime"+i,null));
                map.put("classRoom",settings.getString("classRoom"+i,null));
                map.put("date",settings.getString("date"+i,null));
                exams.add(map);
            }
        }

        readBusFile();
        componentReady = true;
        synchronized (timerTask) {
            timerTask.notifyAll();
        }
    }

    private void readBusFile(){
        Element element = null;
        InputStream inputStream  = null;
        try{
            inputStream = getAssets().open("timetable.xml");
        }catch (Exception e){
            Log.e("WelcomeActivity", "Open bus file failed.");
        }
        DocumentBuilder documentBuilder = null;
        DocumentBuilderFactory documentBuilderFactory = null;
        weekdays_tojinnan = new ArrayList<>();
        weekdays_tobalitai = new ArrayList<>();
        weekends_tojinnan = new ArrayList<>();
        weekends_tobalitai = new ArrayList<>();
        try{
            documentBuilderFactory  = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            element  = document.getDocumentElement();
            NodeList days = element.getChildNodes();
            NodeList weekdays_tojinnan_list = days.item(1).getChildNodes().item(1).getChildNodes();
            NodeList weekdays_tobalitai_list = days.item(1).getChildNodes().item(3).getChildNodes();
            NodeList weekends_tojinnan_list = days.item(3).getChildNodes().item(1).getChildNodes();
            NodeList weekends_tobalitai_list = days.item(3).getChildNodes().item(3).getChildNodes();
            HashMap<String,Integer> tmpMap;
            for(int i = 0;i<weekdays_tojinnan_list.getLength();i++){
                Node node  = weekdays_tojinnan_list.item(i);
                if("busItem".equals(node.getNodeName())){
                    tmpMap = new HashMap<>();
                    tmpMap.put("id",Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
                    tmpMap.put("way",Integer.parseInt(node.getAttributes().getNamedItem("way").getNodeValue()));
                    tmpMap.put("hour",Integer.parseInt(node.getChildNodes().item(1).getTextContent()));
                    tmpMap.put("minute",Integer.parseInt(node.getChildNodes().item(3).getTextContent()));
                    weekdays_tojinnan.add(tmpMap);
                }
            }
            for(int i = 0;i<weekdays_tobalitai_list.getLength();i++){
                Node node  = weekdays_tobalitai_list.item(i);
                if("busItem".equals(node.getNodeName())){
                    tmpMap = new HashMap<>();
                    tmpMap.put("id",Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
                    tmpMap.put("way",Integer.parseInt(node.getAttributes().getNamedItem("way").getNodeValue()));
                    tmpMap.put("hour",Integer.parseInt(node.getChildNodes().item(1).getTextContent()));
                    tmpMap.put("minute",Integer.parseInt(node.getChildNodes().item(3).getTextContent()));
                    weekdays_tobalitai.add(tmpMap);
                }
            }
            for(int i = 0;i<weekends_tojinnan_list.getLength();i++){
                Node node  = weekends_tojinnan_list.item(i);
                if("busItem".equals(node.getNodeName())){
                    tmpMap = new HashMap<>();
                    tmpMap.put("id",Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
                    tmpMap.put("way",Integer.parseInt(node.getAttributes().getNamedItem("way").getNodeValue()));
                    tmpMap.put("hour",Integer.parseInt(node.getChildNodes().item(1).getTextContent()));
                    tmpMap.put("minute",Integer.parseInt(node.getChildNodes().item(3).getTextContent()));
                    weekends_tojinnan.add(tmpMap);
                }
            }
            for(int i = 0;i<weekends_tobalitai_list.getLength();i++){
                Node node  = weekends_tobalitai_list.item(i);
                if("busItem".equals(node.getNodeName())){
                    tmpMap = new HashMap<>();
                    tmpMap.put("id",Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
                    tmpMap.put("way",Integer.parseInt(node.getAttributes().getNamedItem("way").getNodeValue()));
                    tmpMap.put("hour",Integer.parseInt(node.getChildNodes().item(1).getTextContent()));
                    tmpMap.put("minute",Integer.parseInt(node.getChildNodes().item(3).getTextContent()));
                    weekends_tobalitai.add(tmpMap);
                }
            }
        } catch (SAXException|ParserConfigurationException|IOException e) {
            Log.e("WelcomeActivity", e.toString());
        }
    }
}
