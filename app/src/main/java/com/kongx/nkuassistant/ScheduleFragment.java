package com.kongx.nkuassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


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
        if(Information.selectedCourseCount == -1){
            onRefresh();
        }else loadCurriculum();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_activity = null;
    }

    public void onRefresh() {
        new AlertDialog.Builder(m_activity).setTitle("请注意")
                .setMessage("刷新会导致课程信息与教务系统同步，您修改的课程表信息将不再保存。是否要继续刷新？")
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefresh.setRefreshing(true);
                        Connector.getInformation(Connector.RequestType.CURRICULUM,ScheduleFragment.this,null);
                    }
                }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRefresh.setRefreshing(false);
            }
        })
                .show();
    }

    @Override
    public void onConnectorComplete(Connector.RequestType requestType, Object result) {
        if (m_activity == null) return;
        switch (requestType){
            case CURRICULUM:
                if(result.getClass() == Boolean.class && (Boolean)result) {
                    Information.selectedCourses = Connector.tmpSelectedCourses;
                    Information.selectedCourseCount = Connector.tmpSelectedCourses.size();
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
            editor.putInt("dayOfWeek" + i, Information.selectedCourses.get(i).dayOfWeek);
            editor.putInt("startTime" + i, Information.selectedCourses.get(i).startTime);
            editor.putInt("endTime" + i, Information.selectedCourses.get(i).endTime);
            editor.putString("classRoom" + i, Information.selectedCourses.get(i).classRoom);
//            editor.putString("classType" + i, Information.selectedCourses.get(i).classType);
            editor.putString("teacherName" + i, Information.selectedCourses.get(i).teacherName);
            editor.putInt("startWeek" + i, Information.selectedCourses.get(i).startWeek);
            editor.putInt("endWeek" + i, Information.selectedCourses.get(i).endWeek);
            editor.putInt("color" + i, Information.selectedCourses.get(i).color);
        }
        for(int i = 0;i < 14;i++){
            for(int j = 0;j < 7;j++){
                editor.putBoolean("isBusy"+i+j,Information.scheduleTimeIsBusy[i][j]);
            }
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
        int lessonHeight = getResources().getDimensionPixelSize(R.dimen.fragment_class_list_item_height) + margin1Px;
        RelativeLayout.LayoutParams layoutParams;
        TextView courseText;
        int courseHeight, courseWidth;
        for(int i = 0; i < Information.selectedCourseCount; i++){
            courseParent = new RelativeLayout(getActivity());
            courseHeight = lessonHeight * (Information.selectedCourses.get(i).endTime - Information.selectedCourses.get(i).startTime + 1);
            courseWidth = lessonWidth;
            layoutParams = new RelativeLayout.LayoutParams(courseWidth, courseHeight);
            layoutParams.setMargins(marginLessonPx + lessonWidth * (Information.selectedCourses.get(i).dayOfWeek - 1),
                    ((Information.selectedCourses.get(i).startTime - 1) * (lessonHeight)), 0, 0);
            courseParent.setLayoutParams(layoutParams);
            courseText = new TextView(getActivity());
            courseText.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
            courseText.setGravity(Gravity.CENTER);
            courseText.setText(Information.selectedCourses.get(i).name + "（" + Information.selectedCourses.get(i).teacherName + "）" + "@" + Information.selectedCourses.get(i).classRoom);
            courseText.setId(i);
            courseText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(),CourseModifierActivity.class);
                    intent.putExtra("index",view.getId());
                    startActivity(intent);
                }
            });
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
                courseText.setText(Information.selectedCourses.get(i).name + "（" + Information.selectedCourses.get(i).teacherName + "）" + "\n@" + Information.selectedCourses.get(i).classRoom);
                switch (Information.selectedCourses.get(i).color % 8){
                    case 0:
                        courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.curriculum_1));
                        break;
                    case 1:
                        courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.curriculum_2));
                        break;
                    case 2:
                        courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.curriculum_3));
                        break;
                    case 3:
                        courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.curriculum_4));
                        break;
                    case 4:
                        courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.curriculum_5));
                        break;
                    case 5:
                        courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.curriculum_6));
                        break;
                    case 6:
                        courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.curriculum_7));
                        break;
                    case 7:
                        courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.curriculum_8));
                        break;
                    case -1:
                        courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.curriculum_1));
                        break;
                }
            }else{
                courseText.setText("[非本周]" + Information.selectedCourses.get(i).name + "（" + Information.selectedCourses.get(i).teacherName + "）" + "\n@" + Information.selectedCourses.get(i).classRoom);
                courseText.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorNotThisWeekSchedule));
            }
            /** 加入视图*/
            courseParent.addView(courseText);
            mReLayout.addView(courseParent);
        }
        mRefresh.setRefreshing(false);
    }
}
