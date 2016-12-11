package com.kongx.nkuassistant;

import android.support.annotation.NonNull;

import java.nio.Buffer;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

/**
 * Created by kongx on 2016/11/17 0017.
 */

public class Information {
    static String version = "";
    //related to Index Activity
    static final String UPDATE_URL = "http://kongxiao0532.cf/projectnku/update/";
    static final String NOTICE_URL = "http://kongxiao0532.cf/projectnku/notice/";
    static int newestNotice;
    static boolean ifLoggedIn = false;

    //related to Personal Information
    static String name;
    static String facultyName;
    static String majorName;
    static String id;

    //related to Home Page
    static int weekCount;
    static String semester;
    static String date;

    //related to Scores
    static int studiedCourseCount;
    static int selectedCourseCount;
    static ArrayList<HashMap<String,String>> studiedCourses = new ArrayList<>();
    static float[] credits = new float[5];
    static float[] scores = new float[5];
    static float[] averages = new float[5];
    static float credits_All;
    static float scores_All;
    static float average_abcd;
    static float average_abcde;
    static public void resetScores(){
        for(int i = 0;i < 5;i++){
            scores[i] = credits[i] = averages[i] = 0;
        }
        credits_All = scores_All = average_abcd = average_abcde = 0;
    }
    //related to Curriculum
    static ArrayList<HashMap<String,String>> selectedCourses = new ArrayList<>();
    static String curriculum_lastUpdate;


    //related to Exams
    static ArrayList<HashMap<String,String>> exams = new ArrayList<>();
    static int examCount;

    //related to Bus TimeTable
    static ArrayList<HashMap<String,Integer>> weekdays_tojinnan;
    static ArrayList<HashMap<String,Integer>> weekdays_tobalitai;
    static ArrayList<HashMap<String,Integer>> weekends_tojinnan;
    static ArrayList<HashMap<String,Integer>> weekends_tobalitai;
    //related to Feedback
    static final String FEEDBACK_EMAIL = "mailto:kongxiao0532@163.com";
    static final String FEEDBACK_SUBJECT = "[Project NKU] 用户意见反馈";

    //related to Internet Connection
    static boolean ifRemPass;
    static final String PREFS_NAME = "NKUFile";
    static final String COURSE_PREFS_NAME = "CourseFile";
    static final String EXAM_PREFS_NAME = "ExamFile";
    static final String WEB_URL = "http://222.30.49.10";
    public final static class Strings {
        final static String str_pwd_not_changed = "<Not Changed>";
        final static String str_socket_time_out = "请求超时，请重试";
        final static String str_gateway_redirected = "似乎未登录校园网网关？";
        final static String setting_remember_pwd = "ifRemPass";
        final static String setting_studentID = "StudentID";
        final static String setting_password = "Password";
        final static String setting_studied_course_count = "studiedCourseCount";
        final static String setting_selected_course_count = "selectedCourseCount";
        final static String setting_last_update_time = "curriculum_lastUpdate";
        final static String setting_student_name = "StudentName";
        final static String setting_student_faculty = "FacultyName";
        final static String setting_student_major = "MajorName";
        final static String url_template = "operation=&usercode_text=%s&userpwd_text=%s&checkcode_text=%s&submittype=%%C8%%B7+%%C8%%CF";
        final static String url_validate_code = "/ValidateCode";
        final static String url_student_info = "/studymanager/stdbaseinfo/queryAction.do";
        final static String url_webview = "file:///android_asset/encryptpwd.html";
    }
}
