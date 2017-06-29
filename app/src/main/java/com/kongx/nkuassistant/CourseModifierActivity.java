package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kongx.javaclasses.CourseSelected;

import java.util.Collections;

public class CourseModifierActivity extends AppCompatActivity {
    private Button a_button,c_button;
    private EditText mName,mTeacher,mClassroom;
    private Spinner mDayofWeek,mStart,mEnd;
    private int courseIndex;
    private CourseSelected course;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_course_modifier);
        Intent intent =getIntent();
        courseIndex = intent.getIntExtra("courseSelectNum", -1);
        if(courseIndex == -1)   finish();
        mName = (EditText) findViewById(R.id.modifier_name);
        mTeacher = (EditText) findViewById(R.id.modifier_teacher);
        mClassroom = (EditText) findViewById(R.id.modifier_classroom);
        mDayofWeek = (Spinner) findViewById(R.id.modifier_spinner_dayOfWeek);
        mStart = (Spinner) findViewById(R.id.modifier_spinner_startTime);
        mEnd = (Spinner) findViewById(R.id.modifier_spinner_endTime);
        mDayofWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3) {
                ((TextView) arg0.getChildAt(0)).setTextColor(Color.WHITE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        mStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3) {
                ((TextView) arg0.getChildAt(0)).setTextColor(Color.WHITE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        mEnd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3) {
                ((TextView) arg0.getChildAt(0)).setTextColor(Color.WHITE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        a_button = (Button) findViewById(R.id.modifier_accept_button);
        c_button = (Button) findViewById(R.id.modifier_dismiss_button);
        //Comfirm
        a_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mStart.getSelectedItemPosition() > mEnd.getSelectedItemPosition()){
                    Toast.makeText(CourseModifierActivity.this,"课程时间错误",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!checkForConflict(mDayofWeek.getSelectedItemPosition(),mStart.getSelectedItemPosition(),mEnd.getSelectedItemPosition(),course)){
                    Toast.makeText(CourseModifierActivity.this,"课程时间冲突",Toast.LENGTH_SHORT).show();
                    return;
                }
                //storeChanges
                Information.selectedCourses.get(courseIndex).setCourseName(mName.getText().toString());
                Information.selectedCourses.get(courseIndex).setTeacherName(mTeacher.getText().toString());
                Information.selectedCourses.get(courseIndex).setClassRoom(mClassroom.getText().toString());
                Information.selectedCourses.get(courseIndex).setDayOfWeek(mDayofWeek.getSelectedItemPosition() + 1);
                Information.selectedCourses.get(courseIndex).setStartTime(mStart.getSelectedItemPosition() + 1);
                Information.selectedCourses.get(courseIndex).setEndTime(mEnd.getSelectedItemPosition() + 1);
                storeCourse();
                Collections.sort(Information.selectedCourses, new CourseSelected.SelectedCourseComparator());
                Toast.makeText(CourseModifierActivity.this,"课程保存成功",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        //Dismiss
        c_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //set default value
        course = Information.selectedCourses.get(courseIndex);
        mName.setText(course.getCourseName());
        mTeacher.setText(course.getTeacherName());
        mClassroom.setText(course.getClassRoom());
        mDayofWeek.setSelection(course.getDayOfWeek() - 1);
        mStart.setSelection(course.getStartTime() - 1);
        mEnd.setSelection(course.getEndTime() - 1);
    }
    private boolean storeCourse() {
        SharedPreferences settings = getSharedPreferences(Information.COURSE_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("selectedCourseCount", Information.selectedCourseCount);
        editor.putString("courseSelectNum" + courseIndex, Information.selectedCourses.get(courseIndex).getCourseSelectNum());
        editor.putString("name" + courseIndex, Information.selectedCourses.get(courseIndex).getCourseName());
        editor.putInt("dayOfWeek" + courseIndex, Information.selectedCourses.get(courseIndex).getDayOfWeek());
        editor.putInt("startTime" + courseIndex, Information.selectedCourses.get(courseIndex).getStartTime());
        editor.putInt("endTime" + courseIndex, Information.selectedCourses.get(courseIndex).getEndTime());
        editor.putString("classRoom" + courseIndex, Information.selectedCourses.get(courseIndex).getClassRoom());
//            editor.putString("classType" + courseIndex, Information.selectedCourses.get(courseIndex).classType);
        editor.putString("teacherName" + courseIndex, Information.selectedCourses.get(courseIndex).getTeacherName());
        editor.putInt("startWeek" + courseIndex, Information.selectedCourses.get(courseIndex).getStartWeek());
        editor.putInt("endWeek" + courseIndex, Information.selectedCourses.get(courseIndex).getEndWeek());
        editor.putInt("color" + courseIndex, Information.selectedCourses.get(courseIndex).getColor());
        for(int i = 0;i < 14;i++){
            for(int j = 0;j < 7;j++){
                editor.putBoolean("isBusy"+i+j,Information.scheduleTimeIsBusy[i][j]);
            }
        }
        editor.putString("curriculum_lastUpdate",Information.curriculum_lastUpdate);
        return editor.commit();
    }
    private boolean checkForConflict(int dayOfWeek,int startTime,int endTime,CourseSelected originCourse){
        for(int i = startTime;i<=endTime;i++){
            if (dayOfWeek + 1 == originCourse.getDayOfWeek() && (i + 1 >= originCourse.getStartTime() && i + 1 <= originCourse.getEndTime()))
                continue;
            if(Information.scheduleTimeIsBusy[i][dayOfWeek])    return false;
        }
        //rearrange isBusy array
        for (int i = originCourse.getStartTime(); i <= originCourse.getEndTime(); i++) {
            Information.scheduleTimeIsBusy[i - 1][originCourse.getDayOfWeek() - 1] = false;
        }
        for(int i = startTime;i <= endTime;i++){
            Information.scheduleTimeIsBusy[i][dayOfWeek] = true;
        }
        return true;
    }
}
