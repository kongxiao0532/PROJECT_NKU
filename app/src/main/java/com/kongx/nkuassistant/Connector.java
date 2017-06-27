package com.kongx.nkuassistant;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tk.sunrisefox.httprequest.Connect;
import tk.sunrisefox.httprequest.Request;
import tk.sunrisefox.httprequest.Response;
import tk.sunrisefox.htmlparser.HTML;
import tk.sunrisefox.htmlparser.SimpleHTMLParser;

import static android.os.Environment.getExternalStorageDirectory;

public class Connector {
    enum RequestType{
        STATISTIC,
        CHECK_FOR_UPDATE, DOWNLOAD_UPDATE,
        CHECK_FOR_NOTICE,
        LOG_TO_VPN,
        LOGIN,
        USER_INFO,USER_MAJOR_INFO,USER_MINOR_INFO,
        USER_IDS,USER_MAJOR_IDS,USER_MINOR_IDS,
        CURRICULUM,
        SCORE,
        EVALUATE,//TODO:评教
        EXAM,
        LECTURE,
        TV_CHANNEL,
        FEEDBACK,
        LOGOUT
    };
    static String WEB_URL = "http://eamis.nankai.edu.cn";
    final static String login_string_template = "username=%s&password=%s&encodedPassword=&session_locale=zh_CN";
    private final static String currriculum_string_template = "ignoreHead=1&setting.kind=std&startWeek=1&semester.id=%s&ids=%s";
    private final static String url_login = "/eams/login.action";
    private final static String url_curriculum = "/eams/courseTableForStd!courseTable.action";
    private final static String url_score = "/eams/teach/grade/course/person!historyCourseGrade.action?projectType=MAJOR";
    final static String url_logout = "/eams/logout.action";
    private final static String url_vpn_login = "https://221.238.246.69/por/login_psw.csp?sfrnd=2346912324982305";
    private final static String url_student_basic_info = "/eams/stdDetail.action?_=";
    private final static String url_student_major_info = "/eams/stdDetail!innerIndex.action?projectId=1&_=";
    private final static String url_student_minor_info = "/eams/stdDetail!innerIndex.action?projectId=2&_=";
    private final static String url_student_major_ids = "/eams/courseTableForStd!innerIndex.action?projectId=1&_=";
    private final static String url_student_minor_ids = "/eams/courseTableForStd!innerIndex.action?projectId=2&_=";
    private final static String url_double_before_student_info = "/eams/stdDetail!index.action?projectId=2&_=";
    private final static String url_double_after_student_info = "/eams/stdDetail!index.action?projectId=1&_=";
    private final static String api_exam_id = "http://kongxiao0532.cn/projectnku/api/examid.php";
    private final static String url_exam_info = "/eams/stdExam!examTable.action?examBatch.id=%s&_=%s";
    private final static String url_lectures = "http://jz.nankai.edu.cn/latestshow.action";
    private final static String url_livetv_list = "https://tv.byr.cn/mobile/";
    private final static String api_update_get = "http://kongxiao0532.cn/projectnku/api/update.php?isBeta=";
    private final static String api_feedback_post = "http://kongxiao0532.cn/projectnku/api/feedback.php";
    final static String feedback_post_template = "appVersion=%s&userId=%s&topic=%s&content=%s&email=%s";
    private final static String api_statis_post = "http://kongxiao0532.cn/projectnku/api/statis.php";
    private final static String statis_post_template = "appVersion=%s&id=%s";



    static ArrayList<CourseSelected> tmpSelectedCourses;
    private static int curriculumColor = 0;
    private static ArrayList<Lecture> tmpLectures = new ArrayList<Lecture>();
    static int tmpStudiedCourseCount = -1;
    private static long getTimeStamp(){ return System.currentTimeMillis();    }

    public interface Callback{
        void onConnectorComplete(RequestType requestType, Object result);
    }
    public static void getInformation(RequestType requestType, final Connector.Callback uis,@Nullable String strToPost){
        switch (requestType){
            case STATISTIC:
                String tmpString = String.format(statis_post_template,Information.version,Information.id);
                new Request.Builder().url(api_statis_post).post(tmpString,null);
                break;
            case CHECK_FOR_UPDATE:
                new Request.Builder().url(api_update_get + (Information.isBeta ? 1 : 0)).build().send(new UpdateConnector(uis));
                break;
            case CHECK_FOR_NOTICE:
                new Request.Builder().url(Information.NOTICE_URL).build().send(new NoticeConnector(uis));
                break;
            case LOG_TO_VPN:
                new Request.Builder().url(url_vpn_login).post(strToPost,new VPNConnector(uis));
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
                tmpSelectedCourses = new ArrayList<>();
                Information.scheduleTimeIsBusy = new boolean[14][7];
                curriculumColor = 0;
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
            case EXAM:
                new Request.Builder().url(api_exam_id).tag("ID").get(new ExamConnector(uis));
            case LECTURE:
                tmpLectures.clear();
                new Request.Builder().url(url_lectures).get(new LectureConnector(uis));
                break;
            case TV_CHANNEL:
                Information.CCTVChannels = new ArrayList<>();
                Information.LocalChannels = new ArrayList<>();
                new Request.Builder().url(url_livetv_list).get(new TVChannelConnector(uis));
                break;
            case FEEDBACK:
                new Request.Builder().url(api_feedback_post).post(strToPost,new FeedbackConnector(uis));
                break;
            case LOGOUT:
                break;

        }
    }

    public static class ExamConnector implements Connect.Callback{
        Connector.Callback uis;
        public ExamConnector(Connector.Callback uis)  {  this.uis = uis; }
        @Override
        public void onNetworkComplete(Response response) {
            if(response.code()==200){
                switch (response.tag()){
                    case "ID":
                        int id=0;
                        try{
                            id=Integer.parseInt(response.body());
                        }catch (Exception e){e.printStackTrace();}
                        if(id == -1)    return; //kill switch
                        String tmpUrl;
                        tmpUrl = String.format(url_exam_info,id,getTimeStamp());
                        new Request.Builder().url(WEB_URL+tmpUrl).tag("EXAMINFO").get(new ExamConnector(uis));
                        break;
                    case "EXAMINFO":
                        final ArrayList<HashMap<String,String>> tmpExam = new ArrayList<>();
                        String returnString = response.body();
                        if(!returnString.contains("考试安排"))   return;
                        String stringToBeDealt = returnString.substring(returnString.indexOf("</thead>"),returnString.indexOf("</tbody>"));
                        SimpleHTMLParser.parse(stringToBeDealt, new SimpleHTMLParser.Callback() {
                            int count = -1;
                            HashMap<String,String> tmpExamCourse;
                            Pattern pattern;
                            Matcher matcher;
                            @Override
                            public void onTagStart(HTML.Tag tag, HTML.AttributeSet attributeSet) {

                            }

                            @Override
                            public void onText(String text) {
                                if(text.length() == 4 && count == -1){
                                    tmpExamCourse = new HashMap<String, String>();
                                    tmpExamCourse.put("index",text);
                                    count = 1;
                                    return;
                                }
                                switch (count){
                                    case -1:break;
                                    case 1:     //next:CourseName
                                        if(text.equals("exam.time.noArrange")){
                                            count=-1;
                                            break;
                                        }else if(!text.isEmpty()){
                                            tmpExamCourse.put("name",text);
                                            count++;
                                        }
                                        break;
                                    case 2:     //next:ExamType
                                        if(text.equals("exam.time.noArrange")){
                                            count=-1;
                                            break;
                                        }else if(!text.isEmpty()){
                                            tmpExamCourse.put("examType",text);
                                            count++;
                                        }
                                        break;
                                    case 3:     //next:date
                                        if(text.equals("exam.time.noArrange")){
                                            count=-1;
                                            break;
                                        }else if(!text.isEmpty()){
                                            pattern = Pattern.compile("(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)");
                                            matcher = pattern.matcher(text);
                                            if(matcher.find()){
                                                tmpExamCourse.put("date",matcher.group(2)+"月"+matcher.group(3)+"日");
                                            }
                                            count++;
                                        }
                                        break;
                                    case 4:     //next:timeRange
                                        if(text.equals("exam.time.noArrange")){
                                            count=-1;
                                            break;
                                        }else if(!text.isEmpty()){
                                            tmpExamCourse.put("time",text);
                                            count++;
                                        }
                                        break;
                                    case 5:     //next:Classroom
                                        if(text.equals("exam.time.noArrange")){
                                            count=-1;
                                            break;
                                        }else if(!text.isEmpty()){
                                            tmpExamCourse.put("classRoom",text);
                                            count++;
                                        }
                                        break;
                                    case 6:     //next:Seat
                                        if(text.equals("exam.time.noArrange")){
                                            count=-1;
                                            break;
                                        }else if(!text.isEmpty()){
                                            tmpExamCourse.put("seat",text);
                                            count++;
                                        }
                                        break;
                                    case 7:     //next:status
                                        if(text.equals("exam.time.noArrange")){
                                            count=-1;
                                            break;
                                        }else if(!text.isEmpty()){
                                            tmpExamCourse.put("status",text);
                                            tmpExam.add(tmpExamCourse);
                                            count=-1;
                                        }
                                        break;
                                }
                            }

                            @Override
                            public void onTagEnd(HTML.Tag tag) {

                            }
                        });
                        Collections.sort(tmpExam, new Comparator<HashMap<String, String>>() {
                            @Override
                            public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
                                int month1 = 0,month2 = 0,day1 = 0,day2 = 0,hour1 = 0,hour2 = 0;
                                Pattern pattern = Pattern.compile("(\\d\\d)月(\\d\\d)日");
                                Matcher matcher = pattern.matcher(o1.get("date"));
                                if(matcher.find()){
                                    month1 = Integer.parseInt(matcher.group(1));
                                    day1 = Integer.parseInt(matcher.group(2));
                                }
                                matcher = pattern.matcher(o2.get("date"));
                                if(matcher.find()){
                                    month2 = Integer.parseInt(matcher.group(1));
                                    day2 = Integer.parseInt(matcher.group(2));
                                }
                                pattern = Pattern.compile("(\\d\\d):\\d\\d~\\d\\d:\\d\\d");
                                matcher = pattern.matcher(o1.get("time"));
                                if(matcher.find()){
                                    hour1 = Integer.parseInt(matcher.group(1));
                                }
                                matcher = pattern.matcher(o2.get("time"));
                                if(matcher.find()){
                                    hour2 = Integer.parseInt(matcher.group(1));
                                }
                                if(month1 == month2){
                                    if(day1 == day2){
                                        if(hour1 == hour2){
                                            return 0;
                                        }else return hour1 - hour2;
                                    }else return day1 - day2;
                                }else return month1 - month2;
                            }
                        });

                        Information.exams = tmpExam;
                        uis.onConnectorComplete(RequestType.EXAM,uis);
                        break;
                }
            }
        }

        @Override
        public void onNetworkError(Exception exception) {

        }
    }

    public static class UpdateDownloadConnector implements Connect.Callback{
        Connector.Callback uis;
        public UpdateDownloadConnector(Connector.Callback uis)  {  this.uis = uis; }
        @Override
        public void onNetworkComplete(Response response) {
            File apkFile = response.file();
            uis.onConnectorComplete(RequestType.DOWNLOAD_UPDATE,apkFile);
        }

        @Override
        public void onNetworkError(Exception exception) {

        }
    }

    private static class UpdateConnector implements Connect.Callback{
        Connector.Callback uis;
        public UpdateConnector(Connector.Callback uis)  {  this.uis = uis; }
        @Override
        public void onNetworkComplete(Response response) {
//            if(Information.version.contains("beta"))    return;
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response.body());
                String versionNew = jsonObject.getString("MainVersion") + "." + jsonObject.getString("SecondVersion") + "." + jsonObject.getString("ThirdVersion");
                String stringToShow = "新版本："+versionNew+"\n更新包大小："+jsonObject.getString("apkVolume")+"MB\n更新时间："+jsonObject.getString("releaseDate")+"\n更新内容："+jsonObject.getString("updateLog");
                String[] result = new String[]{versionNew,jsonObject.getString("downloadUrl"),stringToShow};
                uis.onConnectorComplete(RequestType.CHECK_FOR_UPDATE,result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

            String tmpHeadline="",tmpContent="",tmpTarget="",tmpTargetVersion="";
            String tmpId;
            if(matcher.find())  tmpId = matcher.group(1);
            else return;
            if (Information.newestNotice != Integer.parseInt(tmpId)) {
                Information.newestNotice = Integer.parseInt(tmpId);
                pattern = Pattern.compile("<headline>(.+)(</headline>)");
                matcher = pattern.matcher(retString);
                if(matcher.find()) tmpHeadline = matcher.group(1);
                pattern = Pattern.compile("<content>(.+)(</content>)");
                matcher = pattern.matcher(retString);
                if(matcher.find()) tmpContent = matcher.group(1);
                pattern = Pattern.compile("<target>(.+)(</target>)");
                matcher = pattern.matcher(retString);
                if(matcher.find())  tmpTarget = matcher.group(1);
                pattern = Pattern.compile("<targetVersion>(.+)(</targetVersion>)");
                matcher = pattern.matcher(retString);
                if(matcher.find())  tmpTargetVersion = matcher.group(1);
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
            else if(response.code() == 403 || response.code() == 502 || response.code() == 504){
                uis.onConnectorComplete(RequestType.LOGIN,false);
            }
        }

        @Override
        public void onNetworkError(Exception exception) {
            if(exception.getClass() == SocketTimeoutException.class){
                uis.onConnectorComplete(RequestType.LOGIN,false);
            }
        }
    }

    private static class VPNConnector implements Connect.Callback{
        Connector.Callback uis;
        private VPNConnector(Connector.Callback uis)  {  this.uis = uis; }

        @Override
        public void onNetworkComplete(Response response) {
            if(response.code() == 302){
                Connect.setDefaultReplaceRules("^http://(eamis.nankai.edu.cn)<>https://221.238.246.69/web/1/http/0/$1<>all http://(jz.nankai.edu.cn)<>https://221.238.246.69/web/1/http/0/$1<>all");
                uis.onConnectorComplete(RequestType.LOGIN,"vpn");
            }else if(response.code() == 200){
                //TODO: ...
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
                    String returnString = response.body();

                    if(!returnString.contains("<th colSpan=\"2\">在校汇总</th>"))   return;
                    //将ReturnString划分为统计信息和下方显示的实际信息两部分
                    String[] stringToBeDealt = new String[]{
                            returnString.substring(returnString.indexOf("<th colSpan=\"2\">在校汇总</th>"),returnString.indexOf("/tr",returnString.indexOf("<th colSpan=\"2\">在校汇总</th>"))),
                            returnString.substring(returnString.indexOf("<tbody",returnString.indexOf(">学分</th>")),returnString.indexOf("</table",returnString.indexOf(">学分</th>")))
                    };
                    //字符串处理匹配
                    dealWithStatisticData(stringToBeDealt[0]);
                    dealWithScoreData(stringToBeDealt[1]);

                    if(Information.studiedCourseAllCount == 0){
                        Information.studiedCourses = new ArrayList<>();
                        uis.onConnectorComplete(RequestType.SCORE,true);
                        return;
                    }
                    else {
                        uis.onConnectorComplete(RequestType.SCORE,true);
                    }
                    break;
                }
            }else if(response.code() == 302){
                String strToPost = String.format(login_string_template, Information.id, Information.password);
                Connector.getInformation(RequestType.LOGIN,uis,strToPost);
            }
        }

        @Override
        public void onNetworkError(Exception exception) {
            if(exception.getClass() == SocketTimeoutException.class){
                uis.onConnectorComplete(RequestType.SCORE,false);
            }
        }

        private void dealWithStatisticData(String statisticString){
            //获取统计数据
            SimpleHTMLParser.parse(statisticString, new SimpleHTMLParser.Callback() {
                int count = 0;
                @Override
                public void onTagStart(HTML.Tag tag, HTML.AttributeSet attributeSet) {

                }

                @Override
                public void onText(String text) {
                    if(count == 3){
                        try {
                            Information.studiedCourseAllCount = Integer.parseInt(text);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    count++;
                }

                @Override
                public void onTagEnd(HTML.Tag tag) {

                }
            });
        }
        private void dealWithScoreData(String scoreString){
            final ArrayList<CourseStudied> tmpScore = new ArrayList<>();
            //获取详细课程信息
            SimpleHTMLParser.parse(scoreString, new SimpleHTMLParser.Callback() {
                boolean isReadingData;
                int singleCourseTextCount = 0;
                CourseStudied tmpCourse;

                @Override
                public void onTagStart(HTML.Tag tag, HTML.AttributeSet attributeSet) {

                }

                @Override
                public void onText(String text) {
                    if(text.isEmpty() && !isReadingData){//在课与课之间的空白数据，忽略
                        return;
                    }else{
                        isReadingData = true;
                        switch (singleCourseTextCount++){
                            case 0:                         //get Semester
                                tmpCourse = new CourseStudied(CourseStudied.DoubleCourseMark.MAJORCOURSE);
                                tmpCourse.setSemester(text);
                                break;
                            case 1: break;
                            case 2:                         //get class ID
                                tmpCourse.classId = text;
                                break;
                            case 3: break;
                            case 4:                         //get selection ID
                                break;
                            case 5: break;
                            case 6:                         //get course name
                                tmpCourse.setName(text);
                                break;
                            case 7: break;
                            case 8:                         //get class type
                                tmpCourse.setClassType(text);
                                break;
                            case 9: break;
                            case 10:                         //get credits
                                try{tmpCourse.credit = Float.parseFloat(text);}
                                catch (Exception e){e.printStackTrace();}
                                break;
                            case 11: break;
                            case 12:                         //get grade
                                break;
                            case 13: break;
                            case 14:                         //get score
                                tmpCourse.setScore(text);
                            case 15: break;
                            case 16:                            //get so-called gpa
                                tmpScore.add(tmpCourse);
                                singleCourseTextCount = 0;
                                isReadingData = false;
                                break;
                        }
                    }
                }

                @Override
                public void onTagEnd(HTML.Tag tag) {

                }
            });
            Information.studiedCourses=tmpScore;
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
                    tmpString = String.format(currriculum_string_template,Information.semesterId,Information.ids_major);
                    new Request.Builder().url(WEB_URL + url_curriculum).tag("MAJOR").post(tmpString,new CurriculumConnector(uis));
                    break;
                case "BEFORE_MINOR":
                    tmpString = String.format(currriculum_string_template,Information.semesterId,Information.ids_minor);
                    new Request.Builder().url(WEB_URL + url_curriculum).tag("MINOR").post(tmpString,new CurriculumConnector(uis));
                    break;
                default:
                    final String returnString = response.body();
                    Log.e("CHECKPOINT0",response.code()+"");

                    if (response.code() == 200 || response.code() == 206) {
                        Log.e("CHECKPOINT1",response.code()+"");
                        Pattern pattern;
                        Matcher matcher;
                        CourseSelected tmpCourse;
                        int startPoint = 0;
                        while (true) {
                            pattern = Pattern.compile("var actTeachers = \\[\\{id:(.+),name:\"(.+)\",lab:(.+)\\}\\];");
                            matcher = pattern.matcher(returnString);
                            if (matcher.find(startPoint)) {
                                tmpCourse = new CourseSelected();
                                startPoint = matcher.end();
                                tmpCourse.teacherName = matcher.group(2);

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

                                pattern = Pattern.compile("\\);\\r\\n.+index =(\\d.*)\\*unitCount\\+(\\d.*);");
                                matcher = pattern.matcher(returnString);
                                if (matcher.find(startPoint)) {
                                    tmpCourse.dayOfWeek = Integer.parseInt(matcher.group(1)) + 1;
                                    tmpCourse.startTime = Integer.parseInt(matcher.group(2)) + 1;
                                }

                                pattern = Pattern.compile("index =(\\d+)\\*unitCount\\+(\\d+);\\r\\n(.+)\\r\\n...[^i]");
                                matcher = pattern.matcher(returnString);
                                if (matcher.find(startPoint)) {
                                    tmpCourse.endTime = Integer.parseInt(matcher.group(2)) + 1;
                                    startPoint = matcher.end();
                                }
                                for(int i = tmpCourse.startTime;i <= tmpCourse.endTime;i++) Information.scheduleTimeIsBusy[i - 1][tmpCourse.dayOfWeek - 1] = true;
                                tmpCourse.color = checkForSameCourse(tmpCourse.name) == -1 ? curriculumColor++ : checkForSameCourse(tmpCourse.name);
                                tmpSelectedCourses.add(tmpCourse);
                            } else {
                                break;
                            }
                        }
                        Collections.sort(tmpSelectedCourses, new Comparator<CourseSelected>() {
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

        private int checkForSameCourse(String name){
            for(CourseSelected tmp : tmpSelectedCourses){
                if(tmp.name.equals(name))   return tmp.color;
            }
            return -1;
        }
    }

    private static class LectureConnector implements Connect.Callback{
        Callback uis;
        public LectureConnector(Callback uis)   {this.uis = uis;}
        @Override
        public void onNetworkComplete(Response response) {
            final String tmpStirng = response.body();
            if(!tmpStirng.contains("<ul class=\"list-ul\">"))  { uis.onConnectorComplete(RequestType.LECTURE,false); return; }
            String returnStirng = tmpStirng.substring(tmpStirng.indexOf("<ul class=\"list-ul\">"),tmpStirng.contains("<!-- ") ? tmpStirng.indexOf("<!-- ") : tmpStirng.indexOf("<div class=\"cright right\">"));
            SimpleHTMLParser.parse(returnStirng, new SimpleHTMLParser.Callback() {
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
                            tmpLecture.setTime(text);
                            break;
                        case 5:
                            tmpLecture.location = text.substring(3,text.length());
                            break;
                        case 6:
                            tmpLecture.setLecturer(text);
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

    private static class TVChannelConnector implements Connect.Callback{
        Callback uis;
        public TVChannelConnector(Callback uis)   {this.uis = uis;}

        @Override
        public void onNetworkComplete(Response response) {
            String returnString  = response.body();
            if(returnString.isEmpty())  return;
            String HDString = returnString.substring(returnString.indexOf("高清频道"),returnString.indexOf("央视标清"));
            String CCTVSDString = returnString.substring(returnString.indexOf("央视标清"),returnString.indexOf("北京标清"));
            String LocalSDString = returnString.substring(returnString.indexOf("北京标清"),returnString.indexOf("PC版"));
            Pattern pattern;
            Matcher matcher;
            LiveFragment.TVChannel tmpChannel;
            //CCTV SD Channels
            pattern = Pattern.compile("<a href=\"\\/\\/tv6.byr.cn\\/hls\\/(.+)\" target=\"_blank\" class=\"btn btn-block btn-primary\">(\\w*-\\d*)(.*)<\\/a>");
            matcher = pattern.matcher(CCTVSDString);
            while(matcher.find()){
                tmpChannel = new LiveFragment.TVChannel();
                tmpChannel.name = matcher.group(2).equals("CCTV-") ? matcher.group(2)+matcher.group(3) : matcher.group(2);
                Log.e("CHANNELNAME",tmpChannel.name);
                tmpChannel.isSDAvailable = true;
                tmpChannel.SDUrl = matcher.group(1);
                Information.CCTVChannels.add(tmpChannel);
            }
//            Collections.sort(Information.CCTVChannels, new Comparator<LiveFragment.TVChannel>() {
//                @Override
//                public int compare(LiveFragment.TVChannel t1, LiveFragment.TVChannel t2) {
//                    return t1.name.compareTo(t2.name);
//                }
//            });
            //Local Channels
            pattern = Pattern.compile("<a href=\"\\/\\/tv6.byr.cn\\/hls\\/(.+)\" target=\"_blank\" class=\"btn btn-block btn-primary\">(.+)<\\/a>");
            matcher = pattern.matcher(LocalSDString);
            while(matcher.find()){
                tmpChannel = new LiveFragment.TVChannel();
                tmpChannel.name = matcher.group(2);
                tmpChannel.isSDAvailable = true;
                tmpChannel.SDUrl = matcher.group(1);
                Information.LocalChannels.add(tmpChannel);
            }
            //HD Channels
            pattern = Pattern.compile("<a href=\"\\/\\/tv6.byr.cn\\/hls\\/(.+)\" target=\"_blank\" class=\"btn btn-block btn-primary\">(.+)<\\/a>");
            matcher = pattern.matcher(HDString);
nxt_line:   while(matcher.find()){
                for(LiveFragment.TVChannel m : Information.CCTVChannels){
                    if(m.name.equals(matcher.group(2).contains("高清") ? matcher.group(2).replace("高清","") : matcher.group(2))){
                        m.isHDAvailable = true;
                        m.HDUrl = matcher.group(1);
                        continue nxt_line;
                    }
                }
                for(LiveFragment.TVChannel m : Information.LocalChannels){
                    if(m.name.equals(matcher.group(2).contains("高清") ? matcher.group(2).replace("高清","") : matcher.group(2))){
                        m.isHDAvailable = true;
                        m.HDUrl = matcher.group(1);
                        continue nxt_line;
                    }
                }
                tmpChannel = new LiveFragment.TVChannel();
                tmpChannel.name = matcher.group(2).contains("高清") ? matcher.group(2).replace("高清","") : matcher.group(2);
                tmpChannel.isHDAvailable = true;
                tmpChannel.HDUrl = matcher.group(1);
                if(tmpChannel.name.contains("CCTV")) Information.CCTVChannels.add(tmpChannel);
                else Information.LocalChannels.add(tmpChannel);
            }
//            Collections.sort(Information.LocalChannels, new Comparator<LiveFragment.TVChannel>() {
//                @Override
//                public int compare(LiveFragment.TVChannel t1, LiveFragment.TVChannel t2) {
//                    return t1.name.compareTo(t2.name);
//                }
//            });
            uis.onConnectorComplete(RequestType.TV_CHANNEL,true);
        }

        @Override
        public void onNetworkError(Exception exception) {

        }
    }

    private static class FeedbackConnector implements Connect.Callback{
        Callback uis;
        public FeedbackConnector(Callback uis)   {this.uis = uis;}

        @Override
        public void onNetworkComplete(Response response) {
            uis.onConnectorComplete(RequestType.FEEDBACK,response.body());
        }

        @Override
        public void onNetworkError(Exception exception) {

        }
    }
}
