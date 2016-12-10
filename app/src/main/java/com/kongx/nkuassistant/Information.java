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
    //realted to Index Activity
    static final String UPDATE_URL = "http://kongxiao0532.cf/projectnku/update/";
    static final String NOTICE_URL = "http://kongxiao0532.cf/projectnku/notice/";
    static int newestNotice;

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
//    static String score_lastUpdate;
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


    //related to Internet Connection
    static boolean ifRemPass;
    static final String PREFS_NAME = "NKUFile";
    static final String COURSE_PREFS_NAME = "CourseFile";
    static final String EXAM_PREFS_NAME = "ExamFile";
    static final String webUrl = "http://222.30.49.10";
}
