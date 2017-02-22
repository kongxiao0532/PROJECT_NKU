package com.kongx.nkuassistant;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.IDNA;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tk.sunrisefox.httprequest.*;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Connector.Callback{
    private SwipeRefreshLayout mReFresh;
    private Activity m_activity;
    //Schedule Module
    private View myView = null;
    private TextView mWeekText;
    private TextView mSememText;
    private TextView mDate;
    private TextView mDay;
    private TextView mScheduleStatus;
    private TextView mScheduleDetail;
    private LinearLayout mScheduleList;
    private int hour;
    private int minute;
    private int year;
    private int weekOfYear;
    private int dayOfWeek;
    private int courseTodayCount;
    private ArrayList<CourseSelected> courseToday;
    //Exam Module
    private TextView mExamStatus;
    private LinearLayout mExamList;
    private TextView mExamDetail;
    //Score Module
    private TextView mScoreStatus;
    private int newStudiedCourseCount;
    private TextView mScoreDetail;
    //Select Module
    private TextView mSelectStatus;
    private TextView mSelectDetail;
    //Bus Module
    private TextView mBusDetail;
    private TextView mBusToBalitai;
    private TextView mBusToJinnan;
    private TextView mBusToBalitaiWay;
    private TextView mBusToJinnanWay;
    //Network Module
    private Pattern pattern;
    private Matcher matcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_home, container, false);
        m_activity = getActivity();
        mReFresh = (SwipeRefreshLayout) myView.findViewById(R.id.home_refresh);
        mReFresh.setOnRefreshListener(this);
        mWeekText = (TextView) myView.findViewById(R.id.textView_weekCount);
        mSememText = (TextView) myView.findViewById(R.id.textView_semester);
        mDate = (TextView) myView.findViewById(R.id.textView_date);
        mDay = (TextView) myView.findViewById(R.id.textView_day);
        mScheduleStatus = (TextView) myView.findViewById(R.id.home_schedule_text);
        mScheduleList = (LinearLayout) myView.findViewById(R.id.home_schedule_list);
        mScheduleDetail = (TextView) myView.findViewById(R.id.home_schedule_details);
        mExamStatus = (TextView) myView.findViewById(R.id.home_exam_text);
        mExamList = (LinearLayout) myView.findViewById(R.id.home_exam_list);
        mExamDetail = (TextView) myView.findViewById(R.id.home_exam_details);
        mScoreStatus = (TextView) myView.findViewById(R.id.home_score_text);
        mScoreDetail = (TextView) myView.findViewById(R.id.home_score_details);
        mSelectStatus = (TextView) myView.findViewById(R.id.home_select_text);
        mSelectDetail = (TextView) myView.findViewById(R.id.home_select_details);
        mBusDetail = (TextView) myView.findViewById(R.id.home_bus_details);
        mBusToBalitai = (TextView) myView.findViewById(R.id.home_bus_jinnan);
        mBusToJinnan = (TextView) myView.findViewById(R.id.home_bus_balitai);
        mBusToBalitaiWay = (TextView) myView.findViewById(R.id.home_bus_jinnan_way);
        mBusToJinnanWay = (TextView) myView.findViewById(R.id.home_bus_balitai_way);
        mScheduleDetail.setOnClickListener((View.OnClickListener) m_activity);
        mExamDetail.setOnClickListener((View.OnClickListener) m_activity);
        mScoreDetail.setOnClickListener((View.OnClickListener) m_activity);
        mSelectDetail.setOnClickListener((View.OnClickListener) m_activity);
        mBusDetail.setOnClickListener((View.OnClickListener) m_activity);
        courseToday = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1;
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE);
        Information.date = dateFormat.format(calendar.getTime());
        if(year == 2017){
            if(weekOfYear == 1 || weekOfYear == 2){
                Information.weekCount = 0;
                Information.semester = "2016-2017 第一学期";
                Information.semesterId = 30;
            }
            if(weekOfYear > 2 && weekOfYear <= 6){
                Information.weekCount = weekOfYear - 2;
                Information.semester = getString(R.string.winter_vacation);
            }
            if(weekOfYear > 6 && weekOfYear <= 22){
                Information.weekCount = weekOfYear - 6;
                Information.semester = "2016-2017 第二学期";
                Information.semesterId = 31;
            }
            if(weekOfYear > 22 && weekOfYear <= 24){
                Information.weekCount = 0;
                Information.semester = "2016-2017 第二学期";
                Information.semesterId = 31;
            }
            if(weekOfYear > 24 && weekOfYear <= 28){
                Information.weekCount = weekOfYear - 24;
                Information.semester = "2016-2017 夏季学期";
                Information.semesterId = 32;
            }
            if(weekOfYear > 28 && weekOfYear <= 36){
                Information.weekCount = weekOfYear - 28;
                Information.semester = getString(R.string.summber_vacation);
            }
        }
        else if(year == 2016){
            if(weekOfYear >= 38 && weekOfYear <= 53){
                Information.weekCount = weekOfYear - 37;
                Information.semester = "2016-2017 第一学期";
                Information.semesterId = 30;
            }
            if(weekOfYear == 54){
                Information.weekCount = 0;
                Information.semester = "2016-2017 第一学期";
                Information.semesterId = 30;
            }
        }
        if(Information.weekCount == 0){
            mWeekText.setText("考试周");
        }
        else {
            mWeekText.setText("第"+String.valueOf(Information.weekCount)+"周");
        }
        mSememText.setText(Information.semester);
        mDate.setText(dateFormat.format(calendar.getTime()));
        mDay.setText(Information.dayOfWeek[dayOfWeek]);
        if(Information.isFirstOpen){
            onRefresh();
            Information.isFirstOpen = false;
        } else{
            updateSchedule();
            updateBus();
            onConnectorComplete(Connector.RequestType.SCORE,true);
//            mSelectStatus.setText(getString(R.string.pull_to_refresh));
        }
        return myView;
    }

    @Override
    public void onConnectorComplete(Connector.RequestType requestType, Object result) {
        if(m_activity == null)  return;
        SharedPreferences settings = m_activity.getSharedPreferences(Information.PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        switch (requestType){
            case LOGIN:
                if(result.getClass() == Boolean.class && (Boolean)result) {                //Login Successfully
                    Toast.makeText(getActivity(), "已重新登录", Toast.LENGTH_SHORT).show();
                    onRefresh();
                }else {
                    Toast.makeText(getActivity(), "重新登录失败", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(m_activity,EduLoginActivity.class));
                    m_activity.finish();
                }
                break;
            case SCORE:
                Information.ifLoggedIn = true;
                if(Information.ids_major == null)   Connector.getInformation(Connector.RequestType.USER_IDS,this,null);
                if (Information.studiedCourses.size() == Information.studiedCourseCount) {
                    mScoreStatus.setText("暂无成绩更新");
                } else {
                    mScoreStatus.setText("有" + Math.abs(Information.studiedCourses.size() - ((Information.studiedCourseCount == -1) ? 0 : Information.studiedCourseCount)) + "条成绩更新");
                }
                break;
            case USER_MAJOR_IDS:
                editor.putString(Information.Strings.setting_student_major_IDs,Information.ids_major);
                editor.apply();
                break;
            case USER_MINOR_IDS:
                editor.putString(Information.Strings.setting_student_minor_IDs,Information.ids_minor);
                editor.apply();
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        m_activity = getActivity();
    }
    @Override
    public void onPause(){
        super.onPause();
        m_activity = null;
    }

    @Override
    public void onRefresh(){
        mReFresh.setRefreshing(true);
        Information.resetScores();
        Connector.getInformation(Connector.RequestType.SCORE,this,null);
//        new Request.Builder().url(Connector.WEB_URL + Connector.url_score).tag(RequestType.SCORE_COUNT).build().send(this);
        try{
            updateSchedule();
            updateBus();
        }catch (IndexOutOfBoundsException e){
            Log.e("HomeFragment","Maybe you changed the fragment too quick.");
        }
        updateExam();
        mReFresh.setRefreshing(false);
    }


    private void updateExam(){
        mExamList.removeAllViews();
        if(Information.examCount == -1){
            mExamStatus.setText("点击“查看详情”更新");
            return;
        }
        if(Information.examCount == 0){
            mExamStatus.setText("暂无考试信息");
            return;
        }
        else {
            mExamList.setVisibility(View.VISIBLE);
            mExamStatus.setVisibility(View.GONE);
            LayoutInflater mInflater = LayoutInflater.from(m_activity);
            for(int i = 0; i< Information.examCount; i++) {
                View view = mInflater.inflate(R.layout.home_schedule_item, null);
                TextView item_name = (TextView) view.findViewById(R.id.home_schedule_item_name);
                TextView item_classroom = (TextView) view.findViewById(R.id.home_schedule_item_classroom);
                item_name.setText(Information.exams.get(i).get("name"));
                item_classroom.setText(Information.exams.get(i).get("date"));
                mExamList.addView(view);
            }
        }
    }

    private void updateSchedule() throws IndexOutOfBoundsException{
        courseToday.clear();
        mScheduleList.removeAllViews();
        if(Information.selectedCourseCount == -1){
            mScheduleStatus.setText("点击“查看详情”更新");
            return;
        }
        if(Information.selectedCourseCount == 0){
            mScheduleStatus.setText("暂无课程信息");
            return;
        }
        if(Information.weekCount == 0 || Information.semester.equals("寒假") || Information.semester.equals("暑假")){
            mScheduleStatus.setText(getString(R.string.no_course_today));
            return;
        }
        courseToday.clear();
        courseTodayCount = 0;
        CourseSelected tmpCourse;
        for(int i = 0;i < Information.selectedCourseCount;i++){
            if(Information.selectedCourses.get(i).dayOfWeek == dayOfWeek &&
                    Information.weekCount >= Information.selectedCourses.get(i).startWeek &&
                    Information.weekCount <= Information.selectedCourses.get(i).endWeek){
                tmpCourse = new CourseSelected(Information.selectedCourses.get(i));
                courseTodayCount++;
                courseToday.add(tmpCourse);
            }
        }
        if (courseTodayCount == 0){
            mScheduleStatus.setText(getString(R.string.no_course_today));
        }
        else {
            mScheduleList.setVisibility(View.VISIBLE);
            mScheduleStatus.setVisibility(View.GONE);
            LayoutInflater mInflater = LayoutInflater.from(m_activity);
            for(int i=0;i<courseTodayCount;i++) {
                View view = mInflater.inflate(R.layout.home_schedule_item, null);
                TextView item_name = (TextView) view.findViewById(R.id.home_schedule_item_name);
                TextView item_classroom = (TextView) view.findViewById(R.id.home_schedule_item_classroom);
                ImageView item_image = (ImageView) view.findViewById(R.id.home_schedule_item_image);
                item_name.setText((courseToday.get(i).name.length() <= 6) ? courseToday.get(i).name : courseToday.get(i).name.substring(0,3) + "..." + courseToday.get(i).name.substring(courseToday.get(i).name.length() - 3,courseToday.get(i).name.length()));
                item_classroom.setText(courseToday.get(i).classRoom);
                if(courseToday.get(i).startTime == 1 ||courseToday.get(i).startTime == 2)   item_image.setImageResource(R.drawable.morning);
                else if(courseToday.get(i).startTime > 2 && courseToday.get(i).startTime < 11)  item_image.setImageResource(R.drawable.noon);
                else if(courseToday.get(i).startTime > 10)  item_image.setImageResource(R.drawable.evening);
                mScheduleList.addView(view);
            }
        }
    }

    private void updateBus(){
        int minute_after = hour * 60 + minute;
        int toJinnanID = -1;
        int toBalitaiID = -1;
        if(dayOfWeek <= 5){
            for(toJinnanID = 0;toJinnanID < Information.weekdays_tojinnan.size();toJinnanID++){
                if(minute_after > Information.weekdays_tojinnan.get(Information.weekdays_tojinnan.size() - 1).get("hour") * 60 + Information.weekdays_tojinnan.get(Information.weekdays_tojinnan.size() - 1).get("minute")){
                    toJinnanID = -1;
                    break;
                }
                if(minute_after < Information.weekdays_tojinnan.get(toJinnanID).get("hour") * 60 + Information.weekdays_tojinnan.get(toJinnanID).get("minute")){
                    break;
                }
            }
            for(toBalitaiID = 0;toBalitaiID < Information.weekdays_tobalitai.size();toBalitaiID++){
                if(minute_after > Information.weekdays_tobalitai.get(Information.weekdays_tobalitai.size() - 1).get("hour") * 60 + Information.weekdays_tobalitai.get(Information.weekdays_tobalitai.size() - 1).get("minute")){
                    toBalitaiID = -1;
                    break;
                }
                if(minute_after < Information.weekdays_tobalitai.get(toBalitaiID).get("hour") * 60 + Information.weekdays_tobalitai.get(toBalitaiID).get("minute")){
                    break;
                }
            }
            if(toJinnanID != -1){
                mBusToJinnan.setText(String.valueOf(Information.weekdays_tojinnan.get(toJinnanID).get("hour"))+":"+
                        ((String.valueOf(Information.weekdays_tojinnan.get(toJinnanID).get("minute")).equals("0")) ? "00":String.valueOf(Information.weekdays_tojinnan.get(toJinnanID).get("minute"))));
                mBusToJinnanWay.setText(Information.weekdays_tojinnan.get(toJinnanID).get("way") == 1 ? "点" : "快");
                mBusToJinnanWay.setVisibility(View.VISIBLE);
            }else{
                mBusToJinnan.setText(getString(R.string.no_available_buses));
                mBusToJinnan.setGravity(View.TEXT_ALIGNMENT_CENTER);
                mBusToJinnanWay.setVisibility(View.GONE);
            }
            if(toBalitaiID != -1){
                mBusToBalitai.setText(String.valueOf(Information.weekdays_tobalitai.get(toBalitaiID).get("hour"))+":"+
                        ((String.valueOf(Information.weekdays_tobalitai.get(toBalitaiID).get("minute")).equals("0")) ? "00":String.valueOf(Information.weekdays_tobalitai.get(toBalitaiID).get("minute"))));
                mBusToBalitaiWay.setText(Information.weekdays_tobalitai.get(toBalitaiID).get("way") == 1 ? "点" : "快");
                mBusToBalitaiWay.setVisibility(View.VISIBLE);
            }else{
                mBusToBalitai.setText(getString(R.string.no_available_buses));
                mBusToBalitai.setGravity(View.TEXT_ALIGNMENT_CENTER);
                mBusToBalitaiWay.setVisibility(View.GONE);
            }
            return;

        }else {
            for(toJinnanID = 0;toJinnanID < Information.weekends_tojinnan.size();toJinnanID++){
                if(minute_after > Information.weekends_tojinnan.get(Information.weekends_tojinnan.size() - 1).get("hour") * 60 + Information.weekends_tojinnan.get(Information.weekends_tojinnan.size() - 1).get("minute")){
                    toJinnanID = -1;
                    break;
                }
                if(minute_after < Information.weekends_tojinnan.get(toJinnanID).get("hour") * 60 + Information.weekends_tojinnan.get(toJinnanID).get("minute")){
                    break;
                }
            }
            for(toBalitaiID = 0;toBalitaiID < Information.weekends_tobalitai.size();toBalitaiID++){
                if(minute_after > Information.weekends_tobalitai.get(Information.weekends_tobalitai.size() - 1).get("hour") * 60 + Information.weekends_tobalitai.get(Information.weekends_tobalitai.size() - 1).get("minute")){
                    toBalitaiID = -1;
                    break;
                }
                if(minute_after < Information.weekends_tobalitai.get(toBalitaiID).get("hour") * 60 + Information.weekends_tobalitai.get(toBalitaiID).get("minute")){
                    break;
                }
            }
            if(toJinnanID != -1){
                mBusToJinnan.setText(String.valueOf(Information.weekends_tojinnan.get(toJinnanID).get("hour"))+":"+
                        ((String.valueOf(Information.weekends_tojinnan.get(toJinnanID).get("minute")).equals("0")) ? "00":String.valueOf(Information.weekends_tojinnan.get(toJinnanID).get("minute"))));
                mBusToJinnanWay.setText(Information.weekends_tojinnan.get(toJinnanID).get("way") == 1 ? "点" : "快");
                mBusToJinnanWay.setVisibility(View.VISIBLE);
            }else{
                mBusToJinnan.setText(getString(R.string.no_available_buses));
                mBusToJinnan.setGravity(View.TEXT_ALIGNMENT_CENTER);
                mBusToJinnanWay.setVisibility(View.GONE);
            }
            if(toBalitaiID != -1){
                mBusToBalitai.setText(String.valueOf(Information.weekends_tobalitai.get(toBalitaiID).get("hour"))+":"+
                        ((String.valueOf(Information.weekends_tobalitai.get(toBalitaiID).get("minute")).equals("0")) ? "00":String.valueOf(Information.weekends_tobalitai.get(toBalitaiID).get("minute"))));
                mBusToBalitaiWay.setText(Information.weekends_tobalitai.get(toBalitaiID).get("way") == 1 ? "点" : "快");
                mBusToBalitaiWay.setVisibility(View.VISIBLE);
            }else{
                mBusToBalitai.setText(getString(R.string.no_available_buses));
                mBusToBalitai.setGravity(View.TEXT_ALIGNMENT_CENTER);
                mBusToBalitaiWay.setVisibility(View.GONE);
            }
            return;
        }
    }
//
//    private static class RequestType {
//        static final String SCORE_COUNT = "Get score count";
//    }

} 

