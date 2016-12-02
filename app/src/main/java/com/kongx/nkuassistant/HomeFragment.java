package com.kongx.nkuassistant;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.icu.text.IDNA;
import android.net.Uri;
import android.os.Bundle;
import android.transition.CircularPropagation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeFragment extends Fragment implements Connectable{
    private static class RequestType{
        static final int getScoreNumber = 1;
        static final int getExamNumber = 2;
        static final int getSelectStatus = 3;
    }
    //Schedule Module
    private View myView = null;
    private TextView mWeekText;
    private TextView mNo;
    private TextView mSememText;
    private TextView mDate;
    private TextView mDay;
    private TextView mScheduleStatus;
    private ListView mScheduleList;
    private int year;
    private int weekOfYear;
    private int dayOfWeek;
    private int courseTodayCount;
    private ArrayList<HashMap<String,String>> courseToday;
    //Exam Module
    private TextView mExamStatus;
    private ListView mExamList;
    //Score Module
    private TextView mScoreStatus;
    private int newStudiedCourseCount;
    //Select Module
    private TextView mSelectStatus;

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
        mWeekText = (TextView) myView.findViewById(R.id.textView_weekCount);
        mNo = (TextView) myView.findViewById(R.id.textView_No_);
        mSememText = (TextView) myView.findViewById(R.id.textView_semester);
        mDate = (TextView) myView.findViewById(R.id.textView_date);
        mDay = (TextView) myView.findViewById(R.id.textView_day);
        mScheduleStatus = (TextView) myView.findViewById(R.id.home_schedule_text);
        mScheduleList = (ListView) myView.findViewById(R.id.home_schedule_list);
        mExamStatus = (TextView) myView.findViewById(R.id.home_exam_text);
        mExamList = (ListView) myView.findViewById(R.id.home_exam_list);
        mScoreStatus = (TextView) myView.findViewById(R.id.home_score_text);
        mSelectStatus = (TextView) myView.findViewById(R.id.home_select_text);
        courseToday = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1;
        Log.e("APP",""+dayOfWeek);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        Information.date = dateFormat.format(calendar.getTime());
        if(year == 2017){
            if(weekOfYear == 1 || weekOfYear == 2){
                Information.weekCount = 0;
                Information.semester = "2016-2017 第一学期";
            }
            if(weekOfYear > 2 || weekOfYear <= 6){
                Information.weekCount = weekOfYear - 2;
                Information.semester = getString(R.string.winter_vacation);
            }
            if(weekOfYear > 6 || weekOfYear <= 22){
                Information.weekCount = weekOfYear - 6;
                Information.semester = "2016-2017 第二学期";
            }
            if(weekOfYear > 22 || weekOfYear <= 24){
                Information.weekCount = 0;
                Information.semester = "2016-2017 第二学期";
            }
            if(weekOfYear > 24|| weekOfYear <= 28){
                Information.weekCount = weekOfYear - 24;
                Information.semester = "2016-2017 夏季学期";
            }
            if(weekOfYear > 28 || weekOfYear <= 36){
                Information.weekCount = weekOfYear - 28;
                Information.semester = "暑假";
            }
        }
        else if(year == 2016){
            if(weekOfYear >= 38 || weekOfYear <= 53){
                Information.weekCount = weekOfYear - 37;
                Information.semester = "2016-2017 第一学期";
            }
            if(weekOfYear == 54){
                Information.weekCount = 0;
                Information.semester = "2016-2017 第一学期";
            }
        }
        if(Information.weekCount == 0){
            mNo.setVisibility(View.INVISIBLE);
            mWeekText.setText("考试");
        }
        else {
            mWeekText.setText(String.valueOf(Information.weekCount));
        }
        mSememText.setText(Information.semester);
        mDate.setText(dateFormat.format(calendar.getTime()));
        mDay.setText(CurriculumFragment.dayOfWeek[dayOfWeek]);
        new Connect(HomeFragment.this, RequestType.getScoreNumber,null).execute(Information.webUrl+"/xsxk/studiedAction.do");
        new Connect(HomeFragment.this, RequestType.getExamNumber,null).execute(Information.webUrl+"/xxcx/stdexamarrange/listAction.do");
        new Connect(HomeFragment.this, RequestType.getSelectStatus,null).execute(Information.webUrl+"/xsxk/selectMianInitAction.do");
        updateSchedule();
        updateBus();
        return myView;
    }

    @Override
    public void onTaskComplete(Object o, int type) {
        if(o.getClass() == BufferedInputStream.class) {
            BufferedInputStream is = (BufferedInputStream) o;
            String returnString = new Scanner(is, "GB2312").useDelimiter("\\A").next();
            switch (type){
                case RequestType.getScoreNumber:{
                    pattern = Pattern.compile("(共 )(.+)( 条记录)");
                    matcher = pattern.matcher(returnString);
                    if (matcher.find()) newStudiedCourseCount = Integer.parseInt(matcher.group(2));
                    if(newStudiedCourseCount == Information.studiedCourseCount){
                        mScoreStatus.setText("暂无成绩更新");
                    }
                    else {
                        mScoreStatus.setText("有"+Math.abs(newStudiedCourseCount - Information.studiedCourseCount)+"条成绩更新");
                    }
                    break;
                }

                case RequestType.getExamNumber:{
                    pattern = Pattern.compile("<strong>(.+)(<\\/strong>)");
                    matcher = pattern.matcher(returnString);
                    if(matcher.find()){
                        if(matcher.group(1).equals("本学期考试安排未发布！")){
                            mExamList.setVisibility(View.GONE);
                            mExamStatus.setText("暂无考试信息");
                        }
                    }
                    else {
                        //TODO: wait until exam schedule shows up
                        mExamList.setVisibility(View.VISIBLE);

                    }
                    updateExam();
                    break;
                }

                case RequestType.getSelectStatus:{
                    Pattern pattern = Pattern.compile("<strong>(.+)(</strong>)");
                    Matcher matcher = pattern.matcher(returnString);
                    if(matcher.find()){
                        if(matcher.group(1).equals("选课系统关闭")){
                           mSelectStatus.setText("选课系统未开放");
                        }
                    }
                    //TODO:Selection Status
                    else mSelectStatus.setText("选课状态");
                    break;
                }

                default:
                    break;
            }
        }else if(o.getClass() == Integer.class){
            Integer code = (Integer)o;
            if(code == 302){
                this.startActivity(new Intent(getActivity(),EduLoginActivity.class));
                getActivity().finish();
            }
        }else if(o.getClass() == SocketTimeoutException.class){
            Log.e("APP","SocketTimeoutException!");
        }
    }

    private void updateExam(){

    }

    private void updateSchedule(){
        if(Information.weekCount == 0 || Information.semester.equals("寒假") || Information.semester.equals("暑假")){
            mScheduleStatus.setText("今天没有课，记得找点事充实自己的生活哦~");
        }
        courseToday.clear();
        courseTodayCount = 0;
        HashMap<String,String> map;
        for(int i = 0;i < Information.selectedCourseCount;i++){
            if(Integer.parseInt(Information.selectedCourses.get(i).get("dayOfWeek")) == dayOfWeek &&
                    Information.weekCount >= Integer.parseInt(Information.selectedCourses.get(i).get("startWeek")) &&
                    Information.weekCount <= Integer.parseInt(Information.selectedCourses.get(i).get("endWeek"))){
                map = new HashMap<>();
                courseTodayCount++;
                map.put("name",Information.selectedCourses.get(i).get("name"));
                map.put("classRoom",Information.selectedCourses.get(i).get("classRoom"));
                courseToday.add(map);
            }
        }
        if (courseTodayCount == 0){
            mScoreStatus.setText("今天没有课哦");
        }
        else {
            mScheduleList.setVisibility(View.VISIBLE);
            mScheduleStatus.setVisibility(View.GONE);
            Log.e("COURSETODAY",courseTodayCount+"");
            mScheduleList.setAdapter(new ScheduleAdapter(HomeFragment.this.getActivity().getApplicationContext()));
        }
    }
    private class ScheduleAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public ScheduleAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object getItem(int position) {
            return courseToday.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView item_name;
            TextView item_classroom;
            View view = mInflater.inflate(R.layout.home_schedule_item, null);
            Log.e("COURSETODAY",position+"");
            Log.e("COURSETODAY",courseToday.get(position).get("name"));
//            if(convertView == null){
//                convertView = mInflater.inflate(R.layout.home_schedule_item,null);
                item_name = (TextView) view.findViewById(R.id.home_schedule_item_name);
                item_classroom = (TextView) view.findViewById(R.id.home_schedule_item_classroom);
//                convertView.setTag(holder);//绑定ViewHolder对象
//            }
//            else{
//                holder = (ViewHolder)convertView.getTag();//取出ViewHolder对象
//            }
            item_name.setText(courseToday.get(position).get("name"));
            item_classroom.setText(courseToday.get(position).get("classRoom"));
            return view;
        }
    }

    private void updateBus(){

    }

}

