package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;

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
        courseIndex = intent.getIntExtra("index",-1);
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
                Information.selectedCourses.get(courseIndex).name = mName.getText().toString();
                Information.selectedCourses.get(courseIndex).teacherName = mTeacher.getText().toString();
                Information.selectedCourses.get(courseIndex).classRoom = mClassroom.getText().toString();
                Information.selectedCourses.get(courseIndex).dayOfWeek = mDayofWeek.getSelectedItemPosition() + 1;
                Information.selectedCourses.get(courseIndex).startTime = mStart.getSelectedItemPosition() + 1;
                Information.selectedCourses.get(courseIndex).endTime = mEnd.getSelectedItemPosition() + 1;
                storeCourse();
                Collections.sort(Information.selectedCourses, new Comparator<CourseSelected>() {
                    @Override
                    public int compare(CourseSelected t1, CourseSelected t2) {
                        if (t1.dayOfWeek == t2.dayOfWeek) {
                            return t1.startTime - t2.startTime;
                        } else {
                            return t1.dayOfWeek - t2.dayOfWeek;
                        }
                    }
                });
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
        mName.setText(course.name);
        mTeacher.setText(course.teacherName);
        mClassroom.setText(course.classRoom);
        mDayofWeek.setSelection(course.dayOfWeek - 1);
        mStart.setSelection(course.startTime - 1);
        mEnd.setSelection(course.endTime - 1);
    }
    private boolean storeCourse() {
        SharedPreferences settings = getSharedPreferences(Information.COURSE_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("selectedCourseCount", Information.selectedCourseCount);
        editor.putString("index" + courseIndex, Information.selectedCourses.get(courseIndex).index);
        editor.putString("name" + courseIndex, Information.selectedCourses.get(courseIndex).name);
        editor.putInt("dayOfWeek" + courseIndex, Information.selectedCourses.get(courseIndex).dayOfWeek);
        editor.putInt("startTime" + courseIndex, Information.selectedCourses.get(courseIndex).startTime);
        editor.putInt("endTime" + courseIndex, Information.selectedCourses.get(courseIndex).endTime);
        editor.putString("classRoom" + courseIndex, Information.selectedCourses.get(courseIndex).classRoom);
//            editor.putString("classType" + courseIndex, Information.selectedCourses.get(courseIndex).classType);
        editor.putString("teacherName" + courseIndex, Information.selectedCourses.get(courseIndex).teacherName);
        editor.putInt("startWeek" + courseIndex, Information.selectedCourses.get(courseIndex).startWeek);
        editor.putInt("endWeek" + courseIndex, Information.selectedCourses.get(courseIndex).endWeek);
        editor.putInt("color" + courseIndex, Information.selectedCourses.get(courseIndex).color);
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
            if(dayOfWeek + 1 == originCourse.dayOfWeek && (i + 1 >= originCourse.startTime && i + 1 <= originCourse.endTime))   continue;
            if(Information.scheduleTimeIsBusy[i][dayOfWeek])    return false;
        }
        //rearrange isBusy array
        for(int i = originCourse.startTime;i<=originCourse.endTime;i++){
            Information.scheduleTimeIsBusy[i - 1][originCourse.dayOfWeek - 1] = false;
        }
        for(int i = startTime;i <= endTime;i++){
            Information.scheduleTimeIsBusy[i][dayOfWeek] = true;
        }
        return true;
    }
}
