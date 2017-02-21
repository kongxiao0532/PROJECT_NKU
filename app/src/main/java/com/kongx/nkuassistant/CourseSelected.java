package com.kongx.nkuassistant;

/**
 * Created by DELL on 2017/2/17 0017.
 */

public class CourseSelected {
    String index;
    String name;
    String classRoom;
    String teacherName;
    String classType;
    int dayOfWeek;
    int startTime;
    int endTime;
    int startWeek;
    int endWeek;
    public CourseSelected(){};
    public CourseSelected(CourseSelected courseSelected){
        this.index = courseSelected.index;
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
}
