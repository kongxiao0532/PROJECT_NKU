package com.kongx.nkuassistant;

import java.util.Comparator;

/**
 * Created by DELL on 2017/2/17 0017.
 */

public class CourseSelected {
    private String courseSelectNum;
    private String name;
    private String classRoom;
    private String teacherName;
    private String classType;
    private int dayOfWeek;
    private int startTime;
    private int endTime;
    private int startWeek;
    private int endWeek;
    private int color;
    public CourseSelected(){};
    public CourseSelected(CourseSelected courseSelected){
        this.courseSelectNum = courseSelected.courseSelectNum;
        this.name = courseSelected.name;
        this.classRoom = courseSelected.classRoom;
        this.teacherName = courseSelected.teacherName;
        this.classType = courseSelected.classType;
        this.dayOfWeek = courseSelected.dayOfWeek;
        this.startTime = courseSelected.startTime;
        this.endTime = courseSelected.endTime;
        this.startWeek = courseSelected.startWeek;
        this.endWeek = courseSelected.endWeek;
    }

    public String getCourseSelectNum() {
        return courseSelectNum;
    }

    public void setCourseSelectNum(String input) {
        courseSelectNum = input;
    }

    public String getCourseName() {
        return name;
    }

    public void setCourseName(String input) {
        name = input;
    }

    public String getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(String input) {
        classRoom = input;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String input) {
        teacherName = input;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String input) {
        classType = input;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int input) {
        dayOfWeek = input;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int input) {
        startTime = input;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int input) {
        endTime = input;
    }

    public int getStartWeek() {
        return startWeek;
    }

    public void setStartWeek(int input) {
        startWeek = input;
    }

    public int getEndWeek() {
        return endWeek;
    }

    public void setEndWeek(int input) {
        endWeek = input;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int input) {
        color = input;
    }

    public static class SelectedCourseComparator implements Comparator<CourseSelected> {

        @Override
        public int compare(CourseSelected t1, CourseSelected t2) {
            if (t1.getDayOfWeek() == t2.dayOfWeek) {
                return t1.startTime - t2.startTime;
            } else {
                return t1.dayOfWeek - t2.dayOfWeek;
            }
        }
    }
}
