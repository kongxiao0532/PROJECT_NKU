package com.kongx.nkuassistant;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.pm.ActivityInfoCompat;
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
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ScheduleFragment extends Fragment implements Connectable, SwipeRefreshLayout.OnRefreshListener {
    private View myView = null;
    private ListViewNoScroll mListView;
    private RelativeLayout mReLayout;
    private int numberOfPages;
    private Activity m_activity;
    private SwipeRefreshLayout mRefresh;
    private String[] curriculumIndex= {"1","2","3","4","5","6","7","8","9","10","11","12","13","14"};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_schedule, container, false);
        mReLayout = (RelativeLayout) myView.findViewById(R.id.schedule_relative_layout);
        mListView = (ListViewNoScroll) myView.findViewById(R.id.list_schedule);
        mListView.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.schedule_index_item,curriculumIndex));
        mRefresh = (SwipeRefreshLayout) myView.findViewById(R.id.schedule_refresh);
        mRefresh.setOnRefreshListener(this);
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
        new Connect(this,1,null).execute(Information.webUrl+"/xsxk/selectedAction.do");
    }
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onTaskComplete(Object o, int type) {
        if(m_activity == null) return;
        if(o == null){
        }else if(o.getClass() == BufferedInputStream.class) {

            BufferedInputStream is = (BufferedInputStream) o;
            Pattern pattern;
            Matcher matcher;
            String returnString = new Scanner(is, "GB2312").useDelimiter("\\A").next();
            if (type == 1) {
                pattern = Pattern.compile("(共 )(\\d)( 页,第)");
                matcher = pattern.matcher(returnString);
                if (matcher.find()) numberOfPages = Integer.parseInt(matcher.group(2));
                pattern = Pattern.compile("(共 )(.+)( 条记录)");
                matcher = pattern.matcher(returnString);
                if (matcher.find())
                    Information.selectedCourseCount = Integer.parseInt(matcher.group(2));
            }
            pattern = Pattern.compile("(<td align=\"center\" class=\"NavText\">)(.*)(\\r\\n)");
            matcher = pattern.matcher(returnString);
            HashMap<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < (type < numberOfPages ? 12 : (Information.selectedCourseCount - (type - 1) * 12)); i++) {
                map = new HashMap<String, String>();
                matcher.find();
                matcher.find();
                map.put("index", matcher.group(2));
                matcher.find();
                matcher.find();
                map.put("name", matcher.group(2));
                matcher.find();
                map.put("dayOfWeek", matcher.group(2));
                matcher.find();
                map.put("startTime", matcher.group(2));
                matcher.find();
                map.put("endTime", matcher.group(2));
                matcher.find();
                map.put("classRoom", matcher.group(2));
                matcher.find();
                map.put("classType", matcher.group(2));
                matcher.find();
                map.put("teacherName", matcher.group(2));
                matcher.find();
                map.put("startWeek", matcher.group(2));
                matcher.find();
                map.put("endWeek", matcher.group(2));
                matcher.find();
                Information.selectedCourses.add(map);
            }
            if (type == numberOfPages) loadCurriculum();
            else
                new Connect(ScheduleFragment.this, ++type, "index=" + type).execute(Information.webUrl + "/xsxk/selectedPageAction.do");
        }else if(o.getClass() == Integer.class){
            Integer code = (Integer)o;
            if(code == 302){
                this.startActivity(new Intent(m_activity,EduLoginActivity.class));
                m_activity.finish();
            }
        }else if(o.getClass() == SocketTimeoutException.class){
            Log.e("APP","SocketTimeoutException!");
        }
    }

    public boolean storeCourses() {
        SharedPreferences settings = m_activity.getSharedPreferences(Information.COURSE_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("selectedCourseCount", Information.selectedCourseCount);
        for (int i = 0; i < Information.selectedCourseCount; i++) {
            editor.putString("index" + i, Information.selectedCourses.get(i).get("index"));
            editor.putString("name" + i, Information.selectedCourses.get(i).get("name"));
            editor.putString("dayOfWeek" + i, Information.selectedCourses.get(i).get("dayOfWeek"));
            editor.putString("startTime" + i, Information.selectedCourses.get(i).get("startTime"));
            editor.putString("endTime" + i, Information.selectedCourses.get(i).get("endTime"));
            editor.putString("classRoom" + i, Information.selectedCourses.get(i).get("classRoom"));
            editor.putString("classType" + i, Information.selectedCourses.get(i).get("classType"));
            editor.putString("teacherName" + i, Information.selectedCourses.get(i).get("teacherName"));
            editor.putString("startWeek" + i, Information.selectedCourses.get(i).get("startWeek"));
            editor.putString("endWeek" + i, Information.selectedCourses.get(i).get("endWeek"));
        }
        return editor.commit();
    }
    private void loadCurriculum(){
        mReLayout.removeAllViewsInLayout();
        mReLayout.addView(mListView);
        storeCourses();
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
            courseHeight = lessonHeight * (Integer.parseInt(Information.selectedCourses.get(i).get("endTime")) - Integer.parseInt(Information.selectedCourses.get(i).get("startTime")) + 1) - 2 * margin1Px;
            courseWidth = lessonWidth - 2 * margin1Px;
            layoutParams = new RelativeLayout.LayoutParams(courseWidth, courseHeight);
            layoutParams.setMargins(marginLessonPx + lessonWidth * (Integer.parseInt(Information.selectedCourses.get(i).get("dayOfWeek")) - 1) + margin1Px,
                    ((Integer.parseInt(Information.selectedCourses.get(i).get("startTime")) - 1) * (lessonHeight +  margin1Px)) + margin1Px, 0, 0);
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
            courseText.setText(Information.selectedCourses.get(i).get("name") + "@" + Information.selectedCourses.get(i).get("classRoom"));
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
            courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorSchedule));
            /** 加入视图*/
            courseParent.addView(courseText);
            mReLayout.addView(courseParent);
        }
        mRefresh.setRefreshing(false);
    }
}
