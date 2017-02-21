package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.IDNA;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tk.sunrisefox.httprequest.Connect;
import tk.sunrisefox.httprequest.Request;
import tk.sunrisefox.httprequest.Response;

/**
 * Created by DELL on 2017/2/19 0019.
 */

public class Connector {
    enum RequestType{
        CHECK_FOR_UPDATE,
        CHECK_FOR_NOTICE,
        LOG_TO_VPN,
        LOGIN,
        USER_INFO,USER_MAJOR_INFO,USER_MINOR_INFO,
        USER_IDS,USER_MAJOR_IDS,USER_MINOR_IDS,
        CURRICULUM,
        SCORE,
        LOGOUT
    };
    static String WEB_URL = "http://eamis.nankai.edu.cn";
    final static String login_string_template = "username=%s&password=%s&encodedPassword=&session_locale=zh_CN";
    private final static String vpn_string_tmplate = "svpn_name=%s&svpn_password=%s";
    final static String currriculum_string_template = "ignoreHead=1&setting.kind=std&startWeek=1&semester.id=%s&ids=%s";
    private final static String url_login = "/eams/login.action";
    final static String url_curriculum = "/eams/courseTableForStd!courseTable.action";
    final static String url_score = "/eams/teach/grade/course/person!historyCourseGrade.action?projectType=MAJOR";
    final static String url_logout = "/eams/logout.action";
    private final static String url_vpn_login = "https://60.29.153.223/por/login_psw.csp?sfrnd=2346912324982305";
    private final static String url_student_basic_info = "/eams/stdDetail.action?_=";
    private final static String url_student_major_info = "/eams/stdDetail!innerIndex.action?projectId=1&_=";
    private final static String url_student_minor_info = "/eams/stdDetail!innerIndex.action?projectId=2&_=";
    private final static String url_student_major_ids = "/eams/courseTableForStd!innerIndex.action?projectId=1&_=";
    private final static String url_student_minor_ids = "/eams/courseTableForStd!innerIndex.action?projectId=2&_=";
    public static ArrayList<CourseSelected> tmpStudiedCourses = new ArrayList<>();
    static long getTimeStamp(){ return System.currentTimeMillis();    }
//    public static class RequestType {
//        static final String CHECK_FOR_UPDATE = "Check for update";
//        static final String LOGIN = "Login";
//        static final String USER_MAJOR_IDS = "User Major ID";
//        static final String USER_MINOR_IDS = "User Minor ID";
//        static final String USER_INFO = "User Info";
//        static final String LOG_TO_VPN = "Login to VPN";
//        static final String CURRICULUM = "Curriculum";
//        static final String SCORE = "Score";
//    }
    public interface Callback{
        void onConnectorComplete(RequestType requestType, Object result);
    }
    public static void getInformation(RequestType requestType, Connector.Callback uis,@Nullable String strToPost){
        String tmpString;
        switch (requestType){
            case CHECK_FOR_UPDATE:
                new Request.Builder().url(Information.UPDATE_URL).build().send(new UpdateConnector(uis));
                break;
            case CHECK_FOR_NOTICE:
                new Request.Builder().url(Information.NOTICE_URL).build().send(new NoticeConnector(uis));
                break;
            case LOG_TO_VPN:
                tmpString = String.format(vpn_string_tmplate,Information.id, Information.password);
                new Request.Builder().url(url_vpn_login).post(tmpString,new VPNConnector(uis));
                break;
            case LOGIN:
                new Request.Builder().url(WEB_URL + url_login).post(strToPost,new LoginConnector(uis));
                break;
            case USER_INFO:
                new Request.Builder().url(WEB_URL + url_student_basic_info + getTimeStamp()).tag("BASIC").get(new InfoConnector(uis));
                break;
            case USER_MAJOR_INFO:
                new Request.Builder().url(WEB_URL + url_student_major_info + getTimeStamp()).tag("MAJOR").get(new InfoConnector(uis));
                break;
            case USER_MINOR_INFO:
                new Request.Builder().url(WEB_URL + url_student_minor_info + getTimeStamp()).tag("MINOR").get(new InfoConnector(uis));
                break;
//            case USER_IDS:
//                new Request.Builder().url(WEB_URL + url_student_major_ids + getTimeStamp()).tag("MAJOR").get(new IdsConnector());
//                if(Information.isDoubleMajor)    new Request.Builder().url(WEB_URL + url_student_minor_ids + getTimeStamp()).tag("MINOR").get(new IdsConnector());
//                break;
            case CURRICULUM:
                tmpStudiedCourses = new ArrayList<>();
                new Request.Builder().url(WEB_URL + url_student_major_ids + getTimeStamp()).tag("MAJOR").get(new IdsConnector(uis));
//                tmpString = String.format(currriculum_string_template,"31",Information.ids_major);
//                new Request.Builder().url(WEB_URL + url_curriculum).post(tmpString,new Connector.CurriculumConnector(uis));
                if(Information.isDoubleMajor) {
                    new Request.Builder().url(WEB_URL + url_student_minor_ids + getTimeStamp()).tag("MINOR").get(new IdsConnector(uis));
//                    tmpString = String.format(currriculum_string_template,"31",Information.ids_minor);
//                    new Request.Builder().url(WEB_URL + url_curriculum).post(tmpString,new Connector.CurriculumConnector(uis));
                }
                break;
            case SCORE:
                Log.e("C",uis.toString());
                new Request.Builder().url(WEB_URL + url_score).post("",new ScoreConnector(uis));
                break;
            case LOGOUT:
                break;

        }
    }
    private static class UpdateConnector implements Connect.Callback{
        Connector.Callback uis;
        public UpdateConnector(Connector.Callback uis)  {  this.uis = uis; }
        @Override
        public void onNetworkComplete(Response response) {
            Pattern pattern;
            Matcher matcher;
            String retString = response.body();
            String versionNew = "";
            String apkSize = "";
            String updateTime = "";
            String updateLog = "";
            String downloadLink = "";
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
            if(matcher.find())  downloadLink = matcher.group(1);
            String stringToShow = "新版本："+versionNew+"\n更新包大小："+apkSize+"\n更新时间："+updateTime+"\n更新内容："+updateLog;
            String[] result = new String[]{versionNew,downloadLink,stringToShow};
            uis.onConnectorComplete(RequestType.CHECK_FOR_UPDATE,result);
        }

        @Override
        public void onNetworkError(Exception exception) {

        }
    }

    private static class NoticeConnector implements Connect.Callback{
        Connector.Callback uis;
        public NoticeConnector(Connector.Callback uis)  {  this.uis = uis; }
        @Override
        public void onNetworkComplete(Response response) {
            String retString = response.body();
            Pattern pattern;
            Matcher matcher;
            pattern = Pattern.compile("<id>([0-9])(</id>)");
            matcher = pattern.matcher(retString);
            matcher.find();
            String tmpId = matcher.group(1);
            if (Information.newestNotice != Integer.parseInt(tmpId)) {
                Information.newestNotice = Integer.parseInt(tmpId);
                pattern = Pattern.compile("<headline>(.+)(</headline>)");
                matcher = pattern.matcher(retString);
                matcher.find();
                String tmpHeadline = matcher.group(1);
                pattern = Pattern.compile("<content>(.+)(</content>)");
                matcher = pattern.matcher(retString);
                matcher.find();
                String tmpContent = matcher.group(1);
                pattern = Pattern.compile("<target>(.+)(</target>)");
                matcher = pattern.matcher(retString);
                matcher.find();
                String tmpTarget = matcher.group(1);
                pattern = Pattern.compile("<targetVersion>(.+)(</targetVersion>)");
                matcher = pattern.matcher(retString);
                matcher.find();
                String tmpTargetVersion = matcher.group(1);
                String[] result = new String[]{tmpId,tmpHeadline,tmpContent,tmpTarget,tmpTargetVersion};
                uis.onConnectorComplete(RequestType.CHECK_FOR_NOTICE,result);
            }
        }

        @Override
        public void onNetworkError(Exception exception) {

        }
    }


    private static class InfoConnector implements Connect.Callback{
        Connector.Callback uis;
        private InfoConnector(Connector.Callback uis)  {  this.uis = uis; }
        @Override
        public void onNetworkComplete(Response response) {
            Pattern pattern;
            Matcher matcher;
            String returnString = response.body();
            switch (response.tag()){
                case "BASIC":
                    Information.isDoubleMajor = !returnString.contains("学籍信息");
                    Connector.getInformation(RequestType.USER_MAJOR_INFO,uis,null);
                    break;
                case "MAJOR":
                    pattern = Pattern.compile("<td width=\"25%\">(.+)</td>");
                    matcher = pattern.matcher(returnString);
                    if (matcher.find()) Information.id = matcher.group(1);
                    pattern = Pattern.compile("<td>(.*)(</td>)");
                    matcher = pattern.matcher(returnString);
                    if (matcher.find()) Information.name = matcher.group(1);
                    matcher.find();
                    matcher.find();
                    matcher.find();
                    matcher.find();
                    matcher.find();
                    matcher.find();
                    matcher.find();
                    if (matcher.find()) Information.facultyName = matcher.group(1);
                    if (matcher.find()) Information.majorName = matcher.group(1);
                    uis.onConnectorComplete(RequestType.USER_MAJOR_INFO,null);
                    break;
                case "MINOR":
                    pattern = Pattern.compile("<td>(.*)(</td>)");
                    matcher = pattern.matcher(returnString);
                    matcher.find();
                    matcher.find();
                    matcher.find();
                    matcher.find();
                    matcher.find();
                    matcher.find();
                    matcher.find();
                    matcher.find();
                    matcher.find();
                    if (matcher.find()) Information.minorName = matcher.group(1);
                    uis.onConnectorComplete(RequestType.USER_MINOR_INFO,null);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onNetworkError(Exception exception) {

        }
    }

    private static class LoginConnector implements Connect.Callback{
        Connector.Callback uis;
        private LoginConnector(Connector.Callback uis)  {  this.uis = uis; }

        @Override
        public void onNetworkComplete(Response response) {
            if(response.code() == 302){
                uis.onConnectorComplete(RequestType.LOGIN,true);
            }else if(response.code() == 200){
                if(response.body().contains("密码错误"))    uis.onConnectorComplete(RequestType.LOGIN,"密码错误");
                else    uis.onConnectorComplete(RequestType.LOGIN, "failed");
            }
        }

        @Override
        public void onNetworkError(Exception exception) {
            if(exception.getClass() == SocketTimeoutException.class){
                Connector.getInformation(RequestType.LOG_TO_VPN,uis,null);      //STException happens, then LOG TO VPN
            }
        }
    }

    //TODO:unfinished
    private static class VPNConnector implements Connect.Callback{
        Connector.Callback uis;
        private VPNConnector(Connector.Callback uis)  {  this.uis = uis; }

        @Override
        public void onNetworkComplete(Response response) {
            if(response.code() == 302){
                uis.onConnectorComplete(RequestType.LOGIN,null);
                Connect.setDefaultReplaceRules("^http://(eamis.nankai.edu.cn)<>https://221.238.246.69/web/1/http/0/$1<>all");
            }else if(response.code() == 200){

            }
        }

        @Override
        public void onNetworkError(Exception exception) {
            if(exception.getClass() == SocketTimeoutException.class){
                Connector.getInformation(RequestType.LOG_TO_VPN,uis,null);
            }
        }
    }

    //TODO: 现在与课程紧密关联。。。
    private static class IdsConnector implements Connect.Callback{
        Connector.Callback uis;
        private IdsConnector(Connector.Callback uis)  {  this.uis = uis; }

        @Override
        public void onNetworkComplete(Response response) {
            Pattern pattern;
            Matcher matcher;
            String returnString = response.body();
            pattern = Pattern.compile("(bg.form.addInput\\(form,\"ids\",\")(.+)(\"\\);)");
            matcher = pattern.matcher(returnString);
//            SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME, 0);
//            SharedPreferences.Editor editor = settings.edit();
//            editor.putString(Information.Strings.setting_studentIDs,Information.ids);
//            editor.apply();
            String strToPost;
            switch (response.tag()){
                case "MAJOR":
                    if (matcher.find()) Information.ids_major = matcher.group(2);
                    strToPost = String.format(currriculum_string_template,"31",Information.ids_major);
                    new Request.Builder().url(WEB_URL + url_curriculum).post(strToPost,new Connector.CurriculumConnector(uis));
                    break;
                case "MINOR":
                    if (matcher.find()) Information.ids_minor = matcher.group(2);
                    strToPost = String.format(currriculum_string_template,"31",Information.ids_minor);
                    new Request.Builder().url(WEB_URL + url_curriculum).post(strToPost,new Connector.CurriculumConnector(uis));
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onNetworkError(Exception exception) {

        }
    }

    private static class ScoreConnector implements Connect.Callback{
        Connector.Callback uis;
        private ScoreConnector(Connector.Callback uis)  {  this.uis = uis; }

        @Override
        public void onNetworkComplete(Response response) {
            if(response.code() == 200){
                ArrayList<CourseStudied> tmpScore = new ArrayList<>();
                String returnString = response.body();
                CourseStudied tmpCourse;
                Pattern pattern;
                Matcher matcher;
                int startPoint = 0;
                pattern = Pattern.compile("</th>\\n(.+)<th>(\\d+)</th>\\n(.+)<th>(.+)</th>");
                matcher = pattern.matcher(returnString);
                if (matcher.find()) {
                    Information.studiedCourseCount = Integer.parseInt(matcher.group(2));
                    Information.credits_All = Float.parseFloat(matcher.group(4));
                }
                try {
                    startPoint = matcher.end();
                }catch (IllegalStateException e){
                    uis.onConnectorComplete(RequestType.SCORE,false);
                    return;
                }
                try {
                    for (int i = 0; i < Information.studiedCourseCount; i++) {
                        tmpCourse = new CourseStudied();
                        pattern = Pattern.compile("<td>(.+)</td>");
                        matcher = pattern.matcher(returnString);
                        if (matcher.find(startPoint)) tmpCourse.setSemester(matcher.group(1));
                        startPoint = matcher.end();

                        pattern = Pattern.compile("<td>(.+)\\t(.+)\\n(.+)</td>");
                        matcher = pattern.matcher(returnString);
                        if (matcher.find(startPoint)) tmpCourse.name = matcher.group(1);
                        startPoint = matcher.end();

                        pattern = Pattern.compile("<td>(.+)</td>.+");
                        matcher = pattern.matcher(returnString);
                        if (matcher.find(startPoint)) tmpCourse.classType = matcher.group(1);

                        pattern = Pattern.compile("\\n.+</td>.+<td>(.+)</td>\\n");
                        matcher = pattern.matcher(returnString);
                        if (matcher.find(startPoint)) tmpCourse.credit = Float.parseFloat(matcher.group(1));
                        startPoint = matcher.end();

                        pattern = Pattern.compile("</td><td style=\"\">.+\\t(.+)\\n");
                        matcher = pattern.matcher(returnString);
                        if (matcher.find(startPoint)) tmpCourse.setScore(matcher.group(1));
                        startPoint = matcher.end();
                        tmpScore.add(tmpCourse);
                    }
                }catch (IllegalStateException e){
                    uis.onConnectorComplete(RequestType.SCORE,false);
                    e.printStackTrace();
                    return;
                }
                Information.studiedCourses = tmpScore;
                uis.onConnectorComplete(RequestType.SCORE,true);
            }else if(response.code() == 302){
                String strToPost = String.format(login_string_template, Information.id, Information.password);
                Connector.getInformation(RequestType.LOGIN,uis,strToPost);
            }
        }

        @Override
        public void onNetworkError(Exception exception) {

        }
    }

    private static class CurriculumConnector implements Connect.Callback{
        Connector.Callback uis;
        private CurriculumConnector(Connector.Callback uis)  {  this.uis = uis; }

        @Override
        public void onNetworkComplete(Response response) {
            final String returnString = response.body();
            if (response.code() == 200 || response.code() == 206) {
                Pattern pattern;
                Matcher matcher;
                CourseSelected tmpCourse;
                int startPoint = 0;
                while (true) {
                    pattern = Pattern.compile("(,name:\")(.+)(\",lab:false\\})");
                    matcher = pattern.matcher(returnString);
                    if (matcher.find(startPoint)) {
                        tmpCourse = new CourseSelected();
                        startPoint = matcher.end();
                        if (matcher.find(startPoint)) {
                            tmpCourse.teacherName = matcher.group(2);
                        }
                        pattern = Pattern.compile("\",\"(.+)\\((\\d+)\\)\",\"\\d+\",\"(.+)\",\"0(\\d+)000000000000000000000000000000000000\"");
                        matcher = pattern.matcher(returnString);
                        if (matcher.find(startPoint)) {
                            tmpCourse.name = matcher.group(1);
                            tmpCourse.index = matcher.group(2);
                            tmpCourse.classRoom = matcher.group(3);
                            String tmpString = matcher.group(4);
                            int duration = 0, startWeek = 1;
                            for (int i = 0; i < tmpString.length(); i++) {
                                if (tmpString.charAt(i) == '1') {
                                    if (duration == 0) startWeek = i + 1;
                                    duration++;
                                }
                            }
                            tmpCourse.startWeek = startWeek;
                            tmpCourse.endWeek = startWeek + duration - 1;
                        }
                        pattern = Pattern.compile("\\);\\n...index =(\\d+)\\*unitCount\\+(\\d+);");
                        matcher = pattern.matcher(returnString);
                        if (matcher.find(startPoint)) {
                            tmpCourse.dayOfWeek = Integer.parseInt(matcher.group(1)) + 1;
                            tmpCourse.startTime = Integer.parseInt(matcher.group(2)) + 1;
                        }
                        pattern = Pattern.compile("index =(\\d+)\\*unitCount\\+(\\d+);\\n(.+)\\n...[^i]");
                        matcher = pattern.matcher(returnString);
                        if (matcher.find(startPoint)) {
                            tmpCourse.endTime = Integer.parseInt(matcher.group(2)) + 1;
                            startPoint = matcher.end();
                        }
                        tmpStudiedCourses.add(tmpCourse);
                    } else {
                        break;
                    }
                }
                Collections.sort(tmpStudiedCourses, new Comparator<CourseSelected>() {
                    @Override
                    public int compare(CourseSelected t1, CourseSelected t2) {
                        if (t1.dayOfWeek == t2.dayOfWeek) {
                            return t1.startTime - t2.startTime;
                        } else {
                            return t1.dayOfWeek - t2.dayOfWeek;
                        }
                    }
                });
                uis.onConnectorComplete(RequestType.CURRICULUM,true);
            }else if(response.code() == 302){
                String strToPost = String.format(login_string_template, Information.id, Information.password);
                Connector.getInformation(RequestType.LOGIN,uis,strToPost);
            }
        }

        @Override
        public void onNetworkError(Exception exception) {

        }
    }
}
