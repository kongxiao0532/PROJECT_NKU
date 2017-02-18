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

import java.io.BufferedInputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tk.sunrisefox.httprequest.Connect;
import tk.sunrisefox.httprequest.Request;
import tk.sunrisefox.httprequest.Response;


public class ScheduleFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Connect.Callback {
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
        stringToPost = String.format(Information.Strings.currriculum_string_template,"31", Information.ids);
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
        m_activity = getActivity();
        if(Information.selectedCourseCount == -1){
            onRefresh();
        }else loadCurriculum();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_activity = null;
    }

    public void onRefresh(){
        mRefresh.setRefreshing(true);
        tmpCurriculumList = new ArrayList<>();
        new Request.Builder().url(Information.WEB_URL + Information.Strings.url_curriculum).requestBody(stringToPost).build().send(this);
    }

    @Override
    public void onNetworkError(Exception exception) {

    }

    @Override
    public void onNetworkComplete(Response response) {
        if(m_activity == null) return;
        if(response.code() == 200) {
            Pattern pattern;
            Matcher matcher;
            String returnString = response.body();
            CourseSelected tmpCourse;
            mNoCurrirulumView.setVisibility(View.GONE);
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
                    tmpCurriculumList.add(tmpCourse);
                } else {
                    break;
                }
            }
            Information.selectedCourseCount = tmpCurriculumList.size();
            update();
        }else if(response.code() == 302){
            startActivity(new Intent(m_activity,EduLoginActivity.class));
            m_activity.finish();
        }
    }

    void update(){
        Collections.sort(tmpCurriculumList, new Comparator<CourseSelected>() {
            @Override
            public int compare(CourseSelected t1,CourseSelected t2) {
                if(t1.dayOfWeek == t2.dayOfWeek){
                    return t1.startTime - t2.startTime;
                }else {
                    return t1.dayOfWeek - t2.dayOfWeek;
                }
            }
        });
        Information.selectedCourses = tmpCurriculumList;
        storeCourses();
        loadCurriculum();
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
