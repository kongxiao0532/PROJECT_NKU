package com.kongx.nkuassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kongx.javaclasses.CourseSelected;
import com.kongx.javaclasses.ExamCourse;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Connector.Callback{
    private Activity m_activity;
    private View myView = null;
    private SwipeRefreshLayout mReFresh;
    //    private TextView mAd;
    private int hour;
    private int minute;
    private int weekOfYear;
    private int dayOfWeek;
//    //Select Module
//    private TextView mSelectStatus;
//    private TextView mSelectDetail;

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

        mReFresh = myView.findViewById(R.id.home_refresh);
        mReFresh.setOnRefreshListener(this);
//        mAd = myView.findViewById(R.id.ad_textview);

//        mSelectStatus = myView.findViewById(R.id.home_select_text);
//        mSelectDetail = myView.findViewById(R.id.home_select_details);
//        mSelectDetail.setOnClickListener((View.OnClickListener) m_activity);

//        if(year == 2017 && Information.month == 4 && Information.day >= 4 && Information.day <= 7)  mAd.setVisibility(View.VISIBLE);
//        mAd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                    Intent intent = new Intent(getActivity(),BrowserActivity.class);
//                    intent.putExtra("url","http://kongxiao0532.cn/kab");
//                    startActivity(intent);
//            }
//        });

        //如果当前未获取过成绩信息，则联网刷新，否则本地刷新
        if (Information.studiedCourses == null) {
            onRefresh();
        } else {
            localRefresh();
        }
        return myView;
    }


    @Override
    public void onConnectorComplete(Connector.RequestType requestType, Object result) {
        if(m_activity == null)  return;
        switch (requestType){
            case LOGIN:
                if(result.getClass() == Boolean.class){
                    if((Boolean)result) {                //Login Successfully
                        Toast.makeText(getActivity(), "已重新登录", Toast.LENGTH_SHORT).show();
                        onRefresh();
                    }else {
                        Toast.makeText(getActivity(), "重新登录失败", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(m_activity,EduLoginActivity.class));
                        m_activity.finish();
                    }
        }
                break;
            case SCORE:
                if(result.getClass() == Boolean.class){
                    if((Boolean)result){
                        if(Information.ids_major == null)   Connector.getInformation(Connector.RequestType.USER_IDS,(Connector.Callback)getActivity(),null);
                        new ScoreSegment();
                    }else {
                        new AlertDialog.Builder(getActivity()).setTitle("登录到南开大学VPN")
                                .setMessage("您请求的网络无法到达，如果您是南开大学在读生，请退回登录界面重新登录。")
                                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SharedPreferences settings = m_activity.getSharedPreferences(Information.PREFS_NAME,0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        Toast.makeText(getActivity().getApplicationContext(), Information.Strings.str_logout_suc , Toast.LENGTH_SHORT).show();
                                        editor.putInt(Information.Strings.setting_selected_course_count,-1);
                                        editor.putInt(Information.Strings.setting_studied_course_count,-1);
                                        editor.putInt(Information.Strings.setting_exam_count,-1);
                                        editor.putString(Information.Strings.setting_student_major_IDs,null);
                                        editor.putString(Information.Strings.setting_student_minor_IDs,null);
                                        editor.apply();
                                        File file = new File(getActivity().getApplicationContext().getApplicationInfo().dataDir,"app_webview/Cookies");
                                        Log.e("APP",file.getAbsolutePath());
                                        file.delete();
                                        Intent intent = new Intent(m_activity.getApplicationContext(), EduLoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        m_activity.finishAffinity();
                                    }
                                })
                                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                    }
                }

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
    public void onRefresh() {            //联网刷新
        mReFresh.setRefreshing(true);
        //本地部分
        new ScheduleSegment();
        new BusSegment();
        new ExamSegment();
        //联网部分
        Connector.getInformation(Connector.RequestType.SCORE,this,null);
        mReFresh.setRefreshing(false);
    }

    private void localRefresh() {
        new ScheduleSegment();
        new BusSegment();
        new ExamSegment();
        new ScoreSegment();
    }

    private class ScheduleSegment {
        //Schedule Module
        private TextView mWeekText;
        private TextView mSememText;
        private TextView mDate;
        private TextView mDay;
        private TextView mScheduleStatus;
        private TextView mScheduleDetail;
        private LinearLayout mScheduleList;

        ScheduleSegment() {
            mWeekText = myView.findViewById(R.id.textView_weekCount);
            mSememText = myView.findViewById(R.id.textView_semester);
            mDate = myView.findViewById(R.id.textView_date);
            mDay = myView.findViewById(R.id.textView_day);
            mScheduleStatus = myView.findViewById(R.id.home_schedule_text);
            mScheduleList = myView.findViewById(R.id.home_schedule_list);
            mScheduleDetail = myView.findViewById(R.id.home_schedule_details);
            mScheduleDetail.setOnClickListener((View.OnClickListener) m_activity);
            setBasicTimeInfo();
            ArrayList<CourseSelected> courseToday = getCourseToday();
            showScheduleToday(courseToday);
        }

        void setBasicTimeInfo() {
            Calendar calendar = Calendar.getInstance();
            Information.year = calendar.get(Calendar.YEAR);
            Information.month = calendar.get(Calendar.MONTH) + 1;
            Information.day = calendar.get(Calendar.DAY_OF_MONTH);
            weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
            Information.dayOfWeek_int = dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1;
            if (Information.dayOfWeek_int == 7) weekOfYear--;
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE);
            Information.date = dateFormat.format(calendar.getTime());

            if (Information.year == 2017) {
                if (weekOfYear == 1 || weekOfYear == 2) {
                    Information.weekCount = 0;
                    Information.semester = "2016-2017 第一学期";
                    Information.semesterId = 30;
                }
                if (weekOfYear > 2 && weekOfYear <= 6) {
                    Information.weekCount = weekOfYear - 2;
                    Information.semester = getString(R.string.winter_vacation);
                }
                if (weekOfYear > 6 && weekOfYear <= 22) {
                    Information.weekCount = weekOfYear - 6;
                    Information.semester = "2016-2017 第二学期";
                    Information.semesterId = 31;
                }
                if (weekOfYear > 22 && weekOfYear <= 24) {
                    Information.weekCount = 0;
                    Information.semester = "2016-2017 第二学期";
                    Information.semesterId = 31;
                }
                if (weekOfYear > 24 && weekOfYear <= 28) {
                    Information.weekCount = weekOfYear - 24;
                    Information.semester = "2016-2017 夏季学期";
                    Information.semesterId = 86;
                }
                if (weekOfYear > 28 && weekOfYear <= 36) {
                    Information.weekCount = weekOfYear - 28;
                    Information.semester = getString(R.string.summber_vacation);
                }
                if (weekOfYear > 36 && weekOfYear <= 52) {
                    Information.weekCount = weekOfYear - 36;
                    Information.semester = "2017-2018 第一学期";
                    Information.semesterId = 62;
                }
                if (weekOfYear == 53) {
                    Information.weekCount = 0;
                    Information.semester = "2017-2018 第一学期";
                    Information.semesterId = 62;
                }
            } else if (Information.year == 2018) {
                if (weekOfYear == 1 || weekOfYear == 2) {
                    Information.weekCount = 0;
                    Information.semester = "2016-2017 第二学期";
                    Information.semesterId = 32;
                }
                if (weekOfYear > 2) {
                    Information.weekCount = weekOfYear - 2;
                    Information.semester = getString(R.string.winter_vacation);
                }
            }
            if (Information.weekCount == 0) {
                mWeekText.setText("考试周");
            } else {
                mWeekText.setText("第" + String.valueOf(Information.weekCount) + "周");
            }
            mSememText.setText(Information.semester);
            mDate.setText(dateFormat.format(calendar.getTime()));
            mDay.setText(Information.dayOfWeek[dayOfWeek]);
        }

        ArrayList<CourseSelected> getCourseToday() {
            if (Information.selectedCourses == null || Information.selectedCourses.size() == 0) {
                mScheduleStatus.setText("暂无课程信息");
                return null;
            }
            if (Information.weekCount == 0 || Information.semester.equals("寒假") || Information.semester.equals("暑假")) {
                mScheduleStatus.setText(getString(R.string.no_course_today));
                return null;
            }
            ArrayList<CourseSelected> courseToday = new ArrayList<>();
            for (CourseSelected tmpCourse : Information.selectedCourses) {
                if (tmpCourse.getDayOfWeek() == dayOfWeek &&
                        Information.weekCount >= tmpCourse.getStartWeek() &&
                        Information.weekCount <= tmpCourse.getEndWeek()) {
                    courseToday.add(tmpCourse);
                }
            }
            return courseToday;
        }

        void showScheduleToday(ArrayList<CourseSelected> courseToday) {
            mScheduleList.removeAllViews();
            if (courseToday == null) return;
            if (courseToday.size() == 0) {
                mScheduleList.setVisibility(View.GONE);
                mScheduleStatus.setVisibility(View.VISIBLE);
                mScheduleStatus.setText(getString(R.string.no_course_today));
            } else {
                mScheduleStatus.setVisibility(View.GONE);
                mScheduleList.setVisibility(View.VISIBLE);
            }
            LayoutInflater mInflater = LayoutInflater.from(m_activity);
            //显示今日课程
            for (CourseSelected tmpCourse : courseToday) {
                View view = mInflater.inflate(R.layout.item_schedule_home_list, null);
                TextView item_name = view.findViewById(R.id.home_schedule_item_name);
                TextView item_classroom = view.findViewById(R.id.home_schedule_item_classroom);
                ImageView item_image = view.findViewById(R.id.home_schedule_item_image);
                item_name.setText((tmpCourse.getCourseName().length() <= 7) ? tmpCourse.getCourseName() : tmpCourse.getCourseName().substring(0, 3) + "..." + tmpCourse.getCourseName().substring(tmpCourse.getCourseName().length() - 3, tmpCourse.getCourseName().length()));
                item_classroom.setText(tmpCourse.getClassRoom());
                //Set Image
                if (tmpCourse.getStartTime() == 1 || tmpCourse.getStartTime() == 2)
                    item_image.setImageResource(R.drawable.morning);
                else if (tmpCourse.getStartTime() > 2 && tmpCourse.getStartTime() < 11)
                    item_image.setImageResource(R.drawable.noon);
                else if (tmpCourse.getStartTime() > 10)
                    item_image.setImageResource(R.drawable.evening);
                mScheduleList.addView(view);
            }
        }
    }

    private class ScoreSegment {
        //Score Module
        private TextView mScoreStatus;
        private TextView mScoreDetail;

        ScoreSegment() {
            mScoreStatus = myView.findViewById(R.id.home_score_text);
            mScoreDetail = myView.findViewById(R.id.home_score_details);
            mScoreDetail.setOnClickListener((View.OnClickListener) m_activity);
            if (Information.studiedCourses != null) {
                if (Information.studiedCourses.size() == Information.lastTimeStudiedCourseCount) {
                    mScoreStatus.setText("暂无成绩更新");
                } else {
                    mScoreStatus.setText("有" + Math.abs(Information.studiedCourses.size() - ((Information.lastTimeStudiedCourseCount == -1) ? 0 : Information.lastTimeStudiedCourseCount)) + "条成绩更新");
                }
            } else {
                mScoreStatus.setText("获取成绩失败");
            }
        }
    }

    private class ExamSegment {
        //Exam Module
        private LinearLayout mExamLayout;
        private TextView mExamStatus;
        private LinearLayout mExamList;
        private TextView mExamDetail;

        ExamSegment() {
            mExamLayout = myView.findViewById(R.id.home_exam_layout);
            mExamStatus = myView.findViewById(R.id.home_exam_text);
            mExamList = myView.findViewById(R.id.home_exam_list);
            mExamDetail = myView.findViewById(R.id.home_exam_details);
            mExamDetail.setOnClickListener((View.OnClickListener) m_activity);
            ArrayList<ExamCourse> examsToBeShown = getExamsToBeShown();
            if (examsToBeShown == null || examsToBeShown.size() == 0) {
                mExamLayout.setVisibility(View.GONE);
            } else {
                mExamLayout.setVisibility(View.VISIBLE);
                mExamList.setVisibility(View.VISIBLE);
                mExamStatus.setVisibility(View.GONE);
                showExams(examsToBeShown);
            }
        }

        ArrayList<ExamCourse> getExamsToBeShown() {
            if (Information.examCount == -1) {
                mExamList.setVisibility(View.GONE);
                mExamStatus.setVisibility(View.VISIBLE);
                mExamStatus.setText("点击“查看详情”更新");
                return null;
            } else {
                ArrayList<ExamCourse> examsToBeShown = new ArrayList<>();
                for (ExamCourse tmpExam : Information.exams) {
                    int month = 0, day = 0;
                    if (tmpExam.getDate() != null) {
                        try {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(tmpExam.getDate());
                            month = cal.get(Calendar.MONTH);
                            day = cal.get(Calendar.DAY_OF_MONTH);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (month < Information.month) continue;
                    else if (month == Information.month && day < Information.day) continue;
                    else examsToBeShown.add(tmpExam);
                }
                return examsToBeShown;
            }
        }

        private void showExams(ArrayList<ExamCourse> examsToBeShown) {
            mExamList.removeAllViews();

            LayoutInflater mInflater = LayoutInflater.from(m_activity);
            for (ExamCourse tmpExam : examsToBeShown) {
                View view = mInflater.inflate(R.layout.item_exam_home_list, null);

                TextView item_name = (TextView) view.findViewById(R.id.home_exam_item_name);
                TextView item_classroom = (TextView) view.findViewById(R.id.home_exam_item_classroom);

                item_name.setText((tmpExam.getCourseName().length() <= 7) ? tmpExam.getCourseName() : tmpExam.getCourseName().substring(0, 3) + "..." + tmpExam.getCourseName().substring(tmpExam.getCourseName().length() - 3, tmpExam.getCourseName().length()));
                item_classroom.setText(tmpExam.getDate().toString());
                mExamList.addView(view);
            }
        }
    }

    private class BusSegment {
        //Bus Module
        private TextView mBusDetail;
        private TextView mBusToBalitai;
        private TextView mBusToJinnan;
        private TextView mBusToBalitaiWay;
        private TextView mBusToJinnanWay;

        BusSegment() {
            mBusDetail = myView.findViewById(R.id.home_bus_details);
            mBusToBalitai = myView.findViewById(R.id.home_bus_jinnan);
            mBusToJinnan = myView.findViewById(R.id.home_bus_balitai);
            mBusToBalitaiWay = myView.findViewById(R.id.home_bus_jinnan_way);
            mBusToJinnanWay = myView.findViewById(R.id.home_bus_balitai_way);
            mBusDetail.setOnClickListener((View.OnClickListener) m_activity);
            calculateNextBus();
        }

        private void calculateNextBus() {
            int minute_after = hour * 60 + minute;
            Information.toJinnanID = -1;
            Information.toBalitaiID = -1;
            if (dayOfWeek <= 5) {
                for (Information.toJinnanID = 0; Information.toJinnanID < Information.weekdays_tojinnan.size(); Information.toJinnanID++) {
                    if (minute_after > Information.weekdays_tojinnan.get(Information.weekdays_tojinnan.size() - 1).get("hour") * 60 + Information.weekdays_tojinnan.get(Information.weekdays_tojinnan.size() - 1).get("minute")) {
                        Information.toJinnanID = -1;
                        break;
                    }
                    if (minute_after < Information.weekdays_tojinnan.get(Information.toJinnanID).get("hour") * 60 + Information.weekdays_tojinnan.get(Information.toJinnanID).get("minute")) {
                        break;
                    }
                }
                for (Information.toBalitaiID = 0; Information.toBalitaiID < Information.weekdays_tobalitai.size(); Information.toBalitaiID++) {
                    if (minute_after > Information.weekdays_tobalitai.get(Information.weekdays_tobalitai.size() - 1).get("hour") * 60 + Information.weekdays_tobalitai.get(Information.weekdays_tobalitai.size() - 1).get("minute")) {
                        Information.toBalitaiID = -1;
                        break;
                    }
                    if (minute_after < Information.weekdays_tobalitai.get(Information.toBalitaiID).get("hour") * 60 + Information.weekdays_tobalitai.get(Information.toBalitaiID).get("minute")) {
                        break;
                    }
                }
                if (Information.toJinnanID != -1) {
                    mBusToJinnan.setText(String.valueOf(Information.weekdays_tojinnan.get(Information.toJinnanID).get("hour")) + ":" +
                            ((String.valueOf(Information.weekdays_tojinnan.get(Information.toJinnanID).get("minute")).equals("0")) ? "00" : String.valueOf(Information.weekdays_tojinnan.get(Information.toJinnanID).get("minute"))));
                    mBusToJinnanWay.setText(Information.weekdays_tojinnan.get(Information.toJinnanID).get("way") == 1 ? "点" : "快");
                    mBusToJinnanWay.setVisibility(View.VISIBLE);
                } else {
                    mBusToJinnan.setText(getString(R.string.no_available_buses));
                    mBusToJinnan.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    mBusToJinnanWay.setVisibility(View.GONE);
                }
                if (Information.toBalitaiID != -1) {
                    mBusToBalitai.setText(String.valueOf(Information.weekdays_tobalitai.get(Information.toBalitaiID).get("hour")) + ":" +
                            ((String.valueOf(Information.weekdays_tobalitai.get(Information.toBalitaiID).get("minute")).equals("0")) ? "00" : String.valueOf(Information.weekdays_tobalitai.get(Information.toBalitaiID).get("minute"))));
                    mBusToBalitaiWay.setText(Information.weekdays_tobalitai.get(Information.toBalitaiID).get("way") == 1 ? "点" : "快");
                    mBusToBalitaiWay.setVisibility(View.VISIBLE);
                } else {
                    mBusToBalitai.setText(getString(R.string.no_available_buses));
                    mBusToBalitai.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    mBusToBalitaiWay.setVisibility(View.GONE);
                }
            } else {
                for (Information.toJinnanID = 0; Information.toJinnanID < Information.weekends_tojinnan.size(); Information.toJinnanID++) {
                    if (minute_after > Information.weekends_tojinnan.get(Information.weekends_tojinnan.size() - 1).get("hour") * 60 + Information.weekends_tojinnan.get(Information.weekends_tojinnan.size() - 1).get("minute")) {
                        Information.toJinnanID = -1;
                        break;
                    }
                    if (minute_after < Information.weekends_tojinnan.get(Information.toJinnanID).get("hour") * 60 + Information.weekends_tojinnan.get(Information.toJinnanID).get("minute")) {
                        break;
                    }
                }
                for (Information.toBalitaiID = 0; Information.toBalitaiID < Information.weekends_tobalitai.size(); Information.toBalitaiID++) {
                    if (minute_after > Information.weekends_tobalitai.get(Information.weekends_tobalitai.size() - 1).get("hour") * 60 + Information.weekends_tobalitai.get(Information.weekends_tobalitai.size() - 1).get("minute")) {
                        Information.toBalitaiID = -1;
                        break;
                    }
                    if (minute_after < Information.weekends_tobalitai.get(Information.toBalitaiID).get("hour") * 60 + Information.weekends_tobalitai.get(Information.toBalitaiID).get("minute")) {
                        break;
                    }
                }
                if (Information.toJinnanID != -1) {
                    mBusToJinnan.setText(String.valueOf(Information.weekends_tojinnan.get(Information.toJinnanID).get("hour")) + ":" +
                            ((String.valueOf(Information.weekends_tojinnan.get(Information.toJinnanID).get("minute")).equals("0")) ? "00" : String.valueOf(Information.weekends_tojinnan.get(Information.toJinnanID).get("minute"))));
                    mBusToJinnanWay.setText(Information.weekends_tojinnan.get(Information.toJinnanID).get("way") == 1 ? "点" : "快");
                    mBusToJinnanWay.setVisibility(View.VISIBLE);
                } else {
                    mBusToJinnan.setText(getString(R.string.no_available_buses));
                    mBusToJinnan.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    mBusToJinnanWay.setVisibility(View.GONE);
                }
                if (Information.toBalitaiID != -1) {
                    mBusToBalitai.setText(String.valueOf(Information.weekends_tobalitai.get(Information.toBalitaiID).get("hour")) + ":" +
                            ((String.valueOf(Information.weekends_tobalitai.get(Information.toBalitaiID).get("minute")).equals("0")) ? "00" : String.valueOf(Information.weekends_tobalitai.get(Information.toBalitaiID).get("minute"))));
                    mBusToBalitaiWay.setText(Information.weekends_tobalitai.get(Information.toBalitaiID).get("way") == 1 ? "点" : "快");
                    mBusToBalitaiWay.setVisibility(View.VISIBLE);
                } else {
                    mBusToBalitai.setText(getString(R.string.no_available_buses));
                    mBusToBalitai.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    mBusToBalitaiWay.setVisibility(View.GONE);
                }

            }
        }
    }
} 

