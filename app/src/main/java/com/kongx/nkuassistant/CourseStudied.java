package com.kongx.nkuassistant;

/**
 * Created by DELL on 2017/2/17 0017.
 */

public class CourseStudied {
    String semester;
    String name;
    String classType;
    float score;
    float credit;
    float[] gpas;
    public void setSemester(String semester){
        this.semester = new String(semester.substring(0,8)+"年第"+semester.charAt(semester.length() - 1)+"学期");
    }
    void setScore(float score){
        this.score = score;
    }
}
