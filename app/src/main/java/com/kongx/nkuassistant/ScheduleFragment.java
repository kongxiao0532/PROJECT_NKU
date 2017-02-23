package com.kongx.nkuassistant;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jiguang.analytics.android.api.JAnalyticsInterface;
import tk.sunrisefox.httprequest.Connect;
import tk.sunrisefox.httprequest.Request;
import tk.sunrisefox.httprequest.Response;


public class ScheduleFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Connector.Callback {
    private View myView = null;
    private ListViewNoScroll mListView;
    private RelativeLayout mReLayout;
    private Activity m_activity;
    private SwipeRefreshLayout mRefresh;
    private ArrayList<CourseSelected> tmpCurriculumList = null;
    private TextView mNoCurrirulumView;
    private String[] curriculumIndex= {"1","2","3","4","5","6","7","8","9","10","11","12","13","14"};
    private String stringToPost;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_schedule, container, false);
        mReLayout = (RelativeLayout) myView.findViewById(R.id.schedule_relative_layout);
        mNoCurrirulumView = (TextView) myView.findViewById(R.id.textView_noSchedule);
        mListView = (ListViewNoScroll) myView.findViewById(R.id.list_schedule);
        mListView.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.schedule_index_item,curriculumIndex));
        mRefresh = (SwipeRefreshLayout) myView.findViewById(R.id.schedule_refresh);
        mRefresh.setOnRefreshListener(this);;
        mNoCurrirulumView.setVisibility(View.GONE);
        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();
        m_activity = getActivity();
        JAnalyticsInterface.onPageStart(m_activity,this.getClass().getCanonicalName());
        if(Information.selectedCourseCount == -1){
            onRefresh();
        }else loadCurriculum();
    }

    @Override
    public void onPause() {
        super.onPause();
        JAnalyticsInterface.onPageEnd(getActivity(),this.getClass().getCanonicalName());
        m_activity = null;
    }

    public void onRefresh() {
        Log.e("SCHEDULE",Information.ids_major+" "+Information.ids_minor);
        mRefresh.setRefreshing(true);
        Connector.getInformation(Connector.RequestType.CURRICULUM,this,null);
    }

    @Override
    public void onConnectorComplete(Connector.RequestType requestType, Object result) {
        if (m_activity == null) return;
        switch (requestType){
            case CURRICULUM:
                if(result.getClass() == Boolean.class && (Boolean)result) {
                    Information.selectedCourses = Connector.tmpStudiedCourses;
                    Information.selectedCourseCount = Connector.tmpStudiedCourses.size();
                    Calendar calendar = Calendar.getInstance();
                    int minute = calendar.get(Calendar.MINUTE);
                    String time_now = String.format(Locale.US, "%2d:%2d", calendar.get(Calendar.HOUR_OF_DAY), minute);
                    Information.curriculum_lastUpdate = Information.date + " " + time_now;
                    storeCourses();
                    mRefresh.setRefreshing(false);
                    loadCurriculum();
                }else if(result.getClass() == Boolean.class && !(Boolean)result) {
                    Toast.makeText(getActivity(),"动作太快啦，请重试",Toast.LENGTH_SHORT).show();
                    mRefresh.setRefreshing(false);
                }
                break;
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
            default:
                break;
        }
    }

    boolean storeCourses() {
        SharedPreferences settings = m_activity.getSharedPreferences(Information.COURSE_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("selectedCourseCount", Information.selectedCourseCount);
        for (int i = 0; i < Information.selectedCourseCount; i++) {
            editor.putString("index" + i, Information.selectedCourses.get(i).index);
            editor.putString("name" + i, Information.selectedCourses.get(i).name);
            editor.putString("dayOfWeek" + i, String.valueOf(Information.selectedCourses.get(i).dayOfWeek));
            editor.putString("startTime" + i, String.valueOf(Information.selectedCourses.get(i).startTime));
            editor.putString("endTime" + i, String.valueOf(Information.selectedCourses.get(i).endTime));
            editor.putString("classRoom" + i, Information.selectedCourses.get(i).classRoom);
//            editor.putString("classType" + i, Information.selectedCourses.get(i).classType);
            editor.putString("teacherName" + i, Information.selectedCourses.get(i).teacherName);
            editor.putString("startWeek" + i, String.valueOf(Information.selectedCourses.get(i).startWeek));
            editor.putString("endWeek" + i, String.valueOf(Information.selectedCourses.get(i).endWeek));
        }
        editor.putString("curriculum_lastUpdate",Information.curriculum_lastUpdate);
        return editor.commit();
    }

    private void loadCurriculum(){
        mReLayout.removeAllViewsInLayout();
        mReLayout.addView(mListView);
        WindowManager manager = getActivity().getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int screenWidth = outMetrics.widthPixels;
        RelativeLayout courseParent;
        int margin1Px = getResources().getDimensionPixelSize(R.dimen.fragment_class_course_margin1dp);
        int marginLessonPx = getResources().getDimensionPixelSize(R.dimen.fragment_class_month_num_width);
        int lessonWidth = (screenWidth - marginLessonPx) / 7;
        int lessonHeight = getResources().getDimensionPixelSize(R.dimen.fragment_class_list_item_height);
        RelativeLayout.LayoutParams layoutParams;
        TextView courseText;
        int courseHeight, courseWidth;
        for(int i = 0; i < Information.selectedCourseCount; i++){
            courseParent = new RelativeLayout(getActivity());
            courseHeight = lessonHeight * (Information.selectedCourses.get(i).endTime - Information.selectedCourses.get(i).startTime + 1) - 2 * margin1Px;
            courseWidth = lessonWidth - 2 * margin1Px;
            layoutParams = new RelativeLayout.LayoutParams(courseWidth, courseHeight);
            layoutParams.setMargins(marginLessonPx + lessonWidth * (Information.selectedCourses.get(i).dayOfWeek - 1) + margin1Px,
                    ((Information.selectedCourses.get(i).startTime - 1) * (lessonHeight +  margin1Px)) + margin1Px, 0, 0);
            courseParent.setLayoutParams(layoutParams);
//            switch (bgImg) {
//                case 1:
//                    courseParent.setBackground(bg1);
//                    break;
//                case 2:
//                    courseParent.setBackground(bg2);
//                    break;
//                case 3:
//                    courseParent.setBackground(bg3);
//                    break;
//                case 4:
//                    courseParent.setBackground(bg4);
//                    break;
//                case 5:
//                    courseParent.setBackground(bg5);
//                    break;
//            }

            courseText = new TextView(getActivity());
            courseText.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
            courseText.setGravity(Gravity.CENTER);
            courseText.setText(Information.selectedCourses.get(i).name + "（" + Information.selectedCourses.get(i).teacherName + "）" + "@" + Information.selectedCourses.get(i).classRoom);
            float textSize;
            textSize=getResources().getDimensionPixelSize(R.dimen.course_text_today_size);

            /**
             * 设置文字大小
             * getTextSize返回值是以像素(px)为单位的，而setTextSize()是以sp为单位的，
             * 因此要这样设置setTextSize(TypedValue.COMPLEX_UNIT_PX, size);*/
            courseText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            /**
             * 设置颜色
             * getResources().getColorStateList()和getColor()读取资源文件设置颜色过时
             * 用如下方法从资源文件设置颜色*/
            courseText.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            if(Information.weekCount >= Information.selectedCourses.get(i).startWeek && Information.weekCount <= Information.selectedCourses.get(i).endWeek){
                courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorSchedule));
            }else               courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorNotThisWeekSchedule));
            /** 加入视图*/
            courseParent.addView(courseText);
            mReLayout.addView(courseParent);
        }
        mRefresh.setRefreshing(false);
    }
}
