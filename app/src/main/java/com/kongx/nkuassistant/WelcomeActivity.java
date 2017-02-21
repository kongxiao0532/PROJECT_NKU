package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cn.jpush.android.api.JPushInterface;
import tk.sunrisefox.httprequest.Connect;

import static com.kongx.nkuassistant.Information.*;


public class WelcomeActivity extends AppCompatActivity {

    private boolean componentReady = false;
    private Intent startIntent = null;
    final TimerTask timerTask = new TimerTask() {
        public void run() {
            synchronized (this) {
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

        //Inflate view
        View v = View.inflate(getApplicationContext(), R.layout.activity_welcome, null);
        setContentView(v);
        new Timer().schedule(timerTask, 2000);

        //Play animation
        ImageView startImg1 = (ImageView) v.findViewById(R.id.startImg);
        ImageView startImg2 = (ImageView) v.findViewById(R.id.startImg2);
        startImg1.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash_fade_in));
        startImg2.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash_move_in));

        JPushInterface.setDebugMode(true);
        JPushInterface.init(getApplication());

        //Initialize network
        System.setProperty("java.net.useSystemProxies", "true");
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        Connect.initialize(cookieManager);
        Connect.setDefaultUA("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36 PROJECT");

        //Get configuration
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        ifRemPass = settings.getBoolean(Strings.setting_remember_pwd, false);

        //Get previous information
        isFirstOpen = true;
        ifLoggedIn = false;
        name = settings.getString(Strings.setting_student_name, "Name");
        facultyName = settings.getString(Strings.setting_student_faculty, "Faculty");
        id = settings.getString(Strings.setting_studentID, "ID");
        newestNotice = settings.getString(Strings.setting_notice, null) == null ? -1 : Integer.parseInt(settings.getString("newestNotice", null));
        password = settings.getString(Strings.setting_password,null);
        //ids/*For new system*/ = settings.getString(Strings.setting_studentIDs, "IDs");
        majorName = settings.getString(Strings.setting_student_major, "Major");
        minorName = settings.getString(Strings.setting_student_minor, "Minor");
        isDoubleMajor = settings.getBoolean(Strings.setting_student_isDoubleMajor,false);
        if (ifRemPass) startIntent = new Intent(this, IndexActivity.class);
        else startIntent = new Intent(this, EduLoginActivity.class);

        //get Curriculum Preferences
        settings = getSharedPreferences(COURSE_PREFS_NAME, 0);
        studiedCourseCount = settings.getInt(Strings.setting_studied_course_count, -1);
        selectedCourseCount = settings.getInt(Strings.setting_selected_course_count, -1);
        curriculum_lastUpdate = settings.getString(Strings.setting_last_update_time, null);
        selectedCourses = new ArrayList<>();
        if (selectedCourseCount != -1) {
            CourseSelected tmpCourse;
            for (int i = 0; i < selectedCourseCount; i++) {
                tmpCourse = new CourseSelected();
                tmpCourse.index = settings.getString("index" + i, "null");
                tmpCourse.name = settings.getString("name" + i, "null");
                tmpCourse.dayOfWeek = Integer.parseInt(settings.getString("dayOfWeek" + i, "0"));
                tmpCourse.startTime = Integer.parseInt(settings.getString("startTime" + i, "0"));
                tmpCourse.endTime = Integer.parseInt(settings.getString("endTime" + i, "0"));
                tmpCourse.classRoom = settings.getString("classRoom" + i, "null");
                tmpCourse.teacherName = settings.getString("teacherName" + i, "null");
                tmpCourse.startWeek = Integer.parseInt(settings.getString("startWeek" + i, "0"));
                tmpCourse.endWeek = Integer.parseInt(settings.getString("endWeek" + i, "0"));
                selectedCourses.add(tmpCourse);
            }
        }

        //get Exams Preferences
        settings = getSharedPreferences(EXAM_PREFS_NAME, 0);
        examCount = settings.getInt(Strings.setting_exam_count, -1);
        if (examCount != -1) {
            HashMap<String, String> map;
            for (int i = 0; i < examCount; i++) {
                map = new HashMap<>();
                map.put("name", settings.getString("name" + i, "null"));
                map.put("startTime", settings.getString("startTime" + i, "null"));
                map.put("endTime", settings.getString("endTime" + i, "null"));
                map.put("classRoom", settings.getString("classRoom" + i, "null"));
                map.put("date", settings.getString("date" + i, "null"));
                exams.add(map);
            }
        }

        readBusFile();
        componentReady = true;
        synchronized (timerTask) {
            timerTask.notifyAll();
        }
    }

    private void readBusFile() {
        Element element = null;
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open("timetable.xml");
        } catch (Exception e) {
            Log.e("WelcomeActivity", "Open bus file failed.");
        }
        DocumentBuilder documentBuilder = null;
        DocumentBuilderFactory documentBuilderFactory = null;
        weekdays_tojinnan = new ArrayList<>();
        weekdays_tobalitai = new ArrayList<>();
        weekends_tojinnan = new ArrayList<>();
        weekends_tobalitai = new ArrayList<>();
        try {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            element = document.getDocumentElement();
            NodeList days = element.getChildNodes();
            NodeList weekdays_tojinnan_list = days.item(1).getChildNodes().item(1).getChildNodes();
            NodeList weekdays_tobalitai_list = days.item(1).getChildNodes().item(3).getChildNodes();
            NodeList weekends_tojinnan_list = days.item(3).getChildNodes().item(1).getChildNodes();
            NodeList weekends_tobalitai_list = days.item(3).getChildNodes().item(3).getChildNodes();
            HashMap<String, Integer> tmpMap;
            for (int i = 0; i < weekdays_tojinnan_list.getLength(); i++) {
                Node node = weekdays_tojinnan_list.item(i);
                if ("busItem".equals(node.getNodeName())) {
                    tmpMap = new HashMap<>();
                    tmpMap.put("id", Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
                    tmpMap.put("way", Integer.parseInt(node.getAttributes().getNamedItem("way").getNodeValue()));
                    tmpMap.put("hour", Integer.parseInt(node.getChildNodes().item(1).getTextContent()));
                    tmpMap.put("minute", Integer.parseInt(node.getChildNodes().item(3).getTextContent()));
                    weekdays_tojinnan.add(tmpMap);
                }
            }
            for (int i = 0; i < weekdays_tobalitai_list.getLength(); i++) {
                Node node = weekdays_tobalitai_list.item(i);
                if ("busItem".equals(node.getNodeName())) {
                    tmpMap = new HashMap<>();
                    tmpMap.put("id", Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
                    tmpMap.put("way", Integer.parseInt(node.getAttributes().getNamedItem("way").getNodeValue()));
                    tmpMap.put("hour", Integer.parseInt(node.getChildNodes().item(1).getTextContent()));
                    tmpMap.put("minute", Integer.parseInt(node.getChildNodes().item(3).getTextContent()));
                    weekdays_tobalitai.add(tmpMap);
                }
            }
            for (int i = 0; i < weekends_tojinnan_list.getLength(); i++) {
                Node node = weekends_tojinnan_list.item(i);
                if ("busItem".equals(node.getNodeName())) {
                    tmpMap = new HashMap<>();
                    tmpMap.put("id", Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
                    tmpMap.put("way", Integer.parseInt(node.getAttributes().getNamedItem("way").getNodeValue()));
                    tmpMap.put("hour", Integer.parseInt(node.getChildNodes().item(1).getTextContent()));
                    tmpMap.put("minute", Integer.parseInt(node.getChildNodes().item(3).getTextContent()));
                    weekends_tojinnan.add(tmpMap);
                }
            }
            for (int i = 0; i < weekends_tobalitai_list.getLength(); i++) {
                Node node = weekends_tobalitai_list.item(i);
                if ("busItem".equals(node.getNodeName())) {
                    tmpMap = new HashMap<>();
                    tmpMap.put("id", Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue()));
                    tmpMap.put("way", Integer.parseInt(node.getAttributes().getNamedItem("way").getNodeValue()));
                    tmpMap.put("hour", Integer.parseInt(node.getChildNodes().item(1).getTextContent()));
                    tmpMap.put("minute", Integer.parseInt(node.getChildNodes().item(3).getTextContent()));
                    weekends_tobalitai.add(tmpMap);
                }
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {
            Log.e("WelcomeActivity", e.toString());
        }
    }
}
