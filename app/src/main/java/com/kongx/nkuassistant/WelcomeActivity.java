package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.InputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InterruptedIOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

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
        readBusFile();
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
    private void readBusFile(){
        Element element = null;
        InputStream inputStream  = null;
        try{ inputStream = getAssets().open("timetable.xml");
        }catch (Exception e){
            e.printStackTrace();
        }
        DocumentBuilder documentBuilder = null;
        DocumentBuilderFactory documentBuilderFactory = null;
        Information.weekdays_tojinnan = new ArrayList<>();
        Information.weekdays_tobalitai = new ArrayList<>();
        Information.weekends_tojinnan = new ArrayList<>();
        Information.weekends_tobalitai = new ArrayList<>();
        try{
            documentBuilderFactory  = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);
            element  = document.getDocumentElement();
            Log.e("BUS",element.getNodeName());
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
                    Information.weekdays_tojinnan.add(tmpMap);
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
                    Information.weekdays_tobalitai.add(tmpMap);
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
                    Information.weekends_tojinnan.add(tmpMap);
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
                    Information.weekends_tobalitai.add(tmpMap);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
