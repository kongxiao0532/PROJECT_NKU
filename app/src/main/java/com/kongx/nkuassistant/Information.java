package com.kongx.nkuassistant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kongx on 2016/11/17 0017.
 */

public class Information {
    static String version = "";
    static String lastVersion = null;
    public static final String[] dayOfWeek = new String[]{"","星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
    public static final String[] startTime = new String[]{"","8:00","8:55","10:00","10:55","12:00","12:55","14:00","14:55","16:00","16:55","18:30","19:25","20:20","21:25"};
    public static final String[] endTime = new String[]{"","8:45","9:40","10:45","11:40","12:45","13:40","14:45","15:40","16:45","17:40","18:30","20:10","21:05","22:00"};
    public static boolean isFirstOpen;

    static String bugCheckFile;

    //related to Index Activity
    static final String UPDATE_URL = "http://kongxiao0532.cn/projectnku/update.html";
    static final String NOTICE_URL = "http://kongxiao0532.cn/projectnku/notice.html";
    static int newestNotice;
    static boolean ifLoggedIn = false;

    //related to Personal Information
    static String name;
    static String facultyName;
    static String majorName;
    static String minorName;
    static String password;
    static String id;
    static String ids_major = null;
    static String ids_minor = null;
    static boolean isDoubleMajor;

    //related to Home Page
    static int dayOfWeek_int;
    static int weekCount;
    static String semester;
    static int semesterId;
    static String date;

    //related to Scores
    static int studiedCourseCount = -1;
    static ArrayList<CourseStudied> studiedCourses = new ArrayList<>();
    static Map<String,Float>  credits         = new HashMap<>();
    static Map<String,Float>  credits_counted = new HashMap<>();
    static Map<String,Float>  scores          = new HashMap<>();
    static Map<String,Float>  averages        = new HashMap<>();
    static float credits_All;
    static float credits_All_counted;
    static float scores_All;
    static float average_abcd;
    static float average_abcde;
    static float[] gpaABCED;
    static float average_f;
    static public void resetScores(){
        credits.clear();
        scores.clear();
        averages.clear();
        credits_counted.clear();
        credits_All_counted = credits_All = scores_All = average_abcd = average_abcde = average_f = 0;
    }
    //related to Curriculum
    static int selectedCourseCount = -1;
    static ArrayList<CourseSelected> selectedCourses = new ArrayList<>();
    static String curriculum_lastUpdate;


    //related to Exams
    static ArrayList<HashMap<String,String>> exams = new ArrayList<>();
    static int examCount = -1;

    //related to Lectures
    static ArrayList<Lecture> lectures = null;

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
    static boolean sessionUseVPN = false;
    static final String PREFS_NAME = "PreferenceFile";
    static final String COURSE_PREFS_NAME = "CourseFile_new";
    static final String EXAM_PREFS_NAME = "ExamFile";
    public final static class Strings {
        final static String setting_notice = "newestNotice";
        final static String str_pwd_not_changed = "<Not Changed>";
        final static String str_socket_time_out = "请求超时，请重试";
        final static String str_gateway_redirected = "似乎未登录校园网网关？";
        final static String str_logout_suc = "退出成功";
        final static String str_logout_failed = "退出失败，请重试";
        final static String str_wrong_password = "密码错误";
        final static String str_login_failed = "登录失败，如果您输错了太多次密码，请重启此程序";
        final static String setting_remember_pwd = "ifRemPass";
        final static String setting_password = "Password";
        final static String setting_studied_course_count = "studiedCourseCount";
        final static String setting_selected_course_count = "selectedCourseCount";
        final static String setting_exam_count = "examCount";
        final static String setting_last_update_time = "curriculum_lastUpdate";
        final static String setting_student_major_IDs = "student_major_IDs";
        final static String setting_student_minor_IDs = "Student_minor_IDs";
        final static String setting_studentID = "StudentID";
        final static String setting_student_name = "StudentName";
        final static String setting_student_faculty = "FacultyName";
        final static String setting_student_major = "MajorName";
        final static String setting_student_minor = "MinorName";
        final static String setting_student_isDoubleMajor = "isDouble";
        final static String setting_last_version = "lastVersion";

    }
}
