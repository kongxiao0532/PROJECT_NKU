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
import tk.sunrisefox.simplehtmlparser.HTML;
import tk.sunrisefox.simplehtmlparser.HTMLParser;

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
        LECTURE,
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
    private final static String url_double_before_student_info = "/eams/stdDetail!index.action?projectId=2&_=";
    private final static String url_double_after_student_info = "/eams/stdDetail!index.action?projectId=1&_=";
    private final static String url_lectures = "http://jz.nankai.edu.cn/latestshow.action";


    static ArrayList<CourseSelected> tmpStudiedCourses = new ArrayList<>();
    static ArrayList<Lecture> tmpLectures = new ArrayList<Lecture>();
    static int tmpStudiedCourseCount = -1;
    static long getTimeStamp(){ return System.currentTimeMillis();    }

    public interface Callback{
        void onConnectorComplete(RequestType requestType, Object result);
    }
    public static void getInformation(RequestType requestType, final Connector.Callback uis,@Nullable String strToPost){
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
                new Request.Builder().url(WEB_URL + url_double_before_student_info + getTimeStamp()).tag("BEFOREMINOR").get(new InfoConnector(uis));
                break;
            case USER_IDS:
                new Request.Builder().url(WEB_URL + url_double_after_student_info + getTimeStamp()).delay(500).tag("BEFORE_MAJOR").get(new IdsConnector(uis));
                break;
            case CURRICULUM:
                tmpStudiedCourses = new ArrayList<>();
                if(Information.ids_major == null)  {
                    uis.onConnectorComplete(RequestType.CURRICULUM,false);
                    return;
                }
                if(Information.isDoubleMajor && Information.ids_minor == null) {
                    uis.onConnectorComplete(RequestType.CURRICULUM,false);
                    return;
                }
                new Request.Builder().url(WEB_URL + url_double_after_student_info + getTimeStamp()).delay(500).tag("BEFORE_MAJOR").get(new CurriculumConnector(uis));
                break;
            case SCORE:
                tmpStudiedCourseCount = Information.studiedCourseCount;
                if(Information.isDoubleMajor)   new Request.Builder().url(WEB_URL + url_double_after_student_info + getTimeStamp()).delay(500).tag("BEFORE_MAJOR").get(new ScoreConnector(uis));
                else    new Request.Builder().url(WEB_URL + url_score).tag("SCORE").post("",new ScoreConnector(uis));
                break;
            case LECTURE:
                tmpLectures.clear();
                new Request.Builder().url(url_lectures).get(new LectureConnector(uis));
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
                case "BEFOREMINOR":
                    new Request.Builder().url(WEB_URL + url_student_minor_info + getTimeStamp()).tag("MINOR").get(new InfoConnector(uis));
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
//                    new Request.Builder().url(WEB_URL + url_double_after_student_info + getTimeStamp()).tag("AFTERMINOR").get(new InfoConnector(uis));
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
            switch (response.tag()){
                case "BEFORE_MAJOR":
                    new Request.Builder().url(WEB_URL + url_student_major_ids + getTimeStamp()).delay(500).tag("MAJOR").get(new IdsConnector(uis));
                    break;
                case "MAJOR":
                    if (matcher.find()) Information.ids_major = matcher.group(2);
                    if(Information.isDoubleMajor)   new Request.Builder().url(WEB_URL + url_double_before_student_info + getTimeStamp()).delay(500).tag("BEFORE_MINOR").get(new IdsConnector(uis));
                    uis.onConnectorComplete(RequestType.USER_MAJOR_IDS,true);
                    break;
                case "BEFORE_MINOR":
                    new Request.Builder().url(WEB_URL + url_student_minor_ids + getTimeStamp()).delay(500).tag("MINOR").get(new IdsConnector(uis));
                    break;
                case "MINOR":
                    if (matcher.find()) Information.ids_minor = matcher.group(2);
                    uis.onConnectorComplete(RequestType.USER_MINOR_IDS,true);
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
            switch (response.tag()){
                case "BEFORE_MAJOR":
                    new Request.Builder().url(WEB_URL + url_score).tag("SCORE").post("",new ScoreConnector(uis));
                    break;
                case "SCORE":
                    ArrayList<CourseStudied> tmpScore = new ArrayList<>();
                    String returnString = response.body();
                    if(!returnString.contains("<th colSpan=\"2\">在校汇总</th>"))   return;
                    String[] stringToBeDealt = new String[]{
                            returnString.substring(returnString.indexOf("<th colSpan=\"2\">在校汇总</th>"),returnString.indexOf("/tr",returnString.indexOf("<th colSpan=\"2\">在校汇总</th>"))),
                            returnString.substring(returnString.indexOf("<tbody",returnString.indexOf(">学分</th>")),returnString.indexOf("</table",returnString.indexOf(">学分</th>")))
                    };
                    HTMLParser.parse(stringToBeDealt[0], new HTMLParser.Callback() {
                        int count = 0;
                        @Override
                        public void onTagStart(HTML.Tag tag, HTML.AttributeSet attributeSet) {

                        }

                        @Override
                        public void onText(String text) {
                            if(count == 3){
                                tmpStudiedCourseCount = Integer.parseInt(text);
                            }else
                            if(count == 5){
                                Information.credits_All = Float.parseFloat(text);
                            }
                            count++;
                        }

                        @Override
                        public void onTagEnd(HTML.Tag tag) {

                        }
                    });
                    if(tmpStudiedCourseCount == 0){
                        Information.studiedCourses = tmpScore;
                        uis.onConnectorComplete(RequestType.SCORE,true);
                        return;
                    }
                    HTMLParser.parse(stringToBeDealt[1], new HTMLParser.Callback() {
                        int textCount = 0, emptyCount = 0;
                        ArrayList<CourseStudied> tmpScore = new ArrayList<>();
                        CourseStudied tmpCourse;

                        @Override
                        public void onTagStart(HTML.Tag tag, HTML.AttributeSet attributeSet) {

                        }

                        @Override
                        public void onText(String text) {
                            if(text.isEmpty()){
                                if(textCount%8 == 0)    return;
                                else if(textCount%8 != emptyCount){
                                    emptyCount++;
                                    return;
                                }
                            }
                            switch ((textCount++)%8){
                                case 0:                         //get Semester
                                    tmpCourse = new CourseStudied();
                                    tmpCourse.setSemester(text);
                                    break;
                                case 1:                         //get class ID
                                    tmpCourse.classId = text;
                                    break;
                                case 2:                         //get selection ID
                                    break;
                                case 3:                         //get course name
                                    tmpCourse.name = text;
                                    break;
                                case 4:                         //get class type
                                    tmpCourse.classType = text;
                                    break;
                                case 5:                         //get credits
                                    tmpCourse.credit = Float.parseFloat(text);
                                    break;
                                case 6:                         //get grade
                                    break;
                                case 7:                         //get score
                                    tmpCourse.setScore(text);
                                    tmpScore.add(tmpCourse);
                                    emptyCount = 0;
                                    break;
                            }
                            if(textCount == tmpStudiedCourseCount * 8){
                                Information.studiedCourses = tmpScore;
                                uis.onConnectorComplete(RequestType.SCORE,true);
                            }
                        }

                        @Override
                        public void onTagEnd(HTML.Tag tag) {

                        }
                    });

                    break;
                }
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
            String tmpString;
            switch (response.tag()){
                case "BEFORE_MAJOR":
                    tmpString = String.format(currriculum_string_template,"31",Information.ids_major);
                    new Request.Builder().url(WEB_URL + url_curriculum).tag("MAJOR").post(tmpString,new CurriculumConnector(uis));
                    break;
                case "BEFORE_MINOR":
                    tmpString = String.format(currriculum_string_template,"31",Information.ids_minor);
                    new Request.Builder().url(WEB_URL + url_curriculum).tag("MINOR").post(tmpString,new CurriculumConnector(uis));
                    break;
                default:
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
                                    tmpString = matcher.group(4);
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
                    if(response.tag().equals("MAJOR")){
                        if(Information.isDoubleMajor) {
                            if(Information.ids_minor == null)    return;
                            new Request.Builder().url(WEB_URL + url_double_before_student_info + getTimeStamp()).delay(500).tag("BEFORE_MINOR").get(new CurriculumConnector(uis));                        }
                    }
                    break;
            }
        }

        @Override
        public void onNetworkError(Exception exception) {

        }
    }

    private static class LectureConnector implements Connect.Callback{
        Callback uis;
        public LectureConnector(Callback uis)   {this.uis = uis;}
        @Override
        public void onNetworkComplete(Response response) {
            final String tmpStirng = response.body();
            if(!tmpStirng.contains("<ul class=\"list-ul\">"))   return;
            String returnStirng = tmpStirng.substring(tmpStirng.indexOf("<ul class=\"list-ul\">"),tmpStirng.contains("<!-- ") ? tmpStirng.indexOf("<!-- ") : tmpStirng.indexOf("<div class=\"cright right\">"));
            HTMLParser.parse(returnStirng, new HTMLParser.Callback() {
                int count = 0;
                Lecture tmpLecture;
                @Override
                public void onTagStart(HTML.Tag tag, HTML.AttributeSet attributeSet) {

                }

                @Override
                public void onText(String text) {
                    if(text.isEmpty())  return;
                    switch (count++%7){
                        case 3:
                            tmpLecture = new Lecture();
                            tmpLecture.topic = text;
                            break;
                        case 4:
                            text = text.replace("&nbsp;","   ");
                            tmpLecture.time = text.substring(3,text.length());
                            break;
                        case 5:
                            tmpLecture.location = text.substring(3,text.length());
                            break;
                        case 6:
                           if(text.contains("\n"))  text = text.replace("\n","");
                            tmpLecture.lecturer = text.substring(4,text.length());
                            tmpLectures.add(tmpLecture);
                            break;
                    }
                }

                @Override
                public void onTagEnd(HTML.Tag tag) {

                }
            });
            Information.lectures = tmpLectures;
            uis.onConnectorComplete(RequestType.LECTURE,true);
        }

        @Override
        public void onNetworkError(Exception exception) {

        }
    }
}
