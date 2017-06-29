package com.kongx.nkuassistant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kongx on 2017/6/28 0028.
 */

public class Exams {
    private String courseSelectNum;
    private Date date;
    private String timePeriod;
    private String courseName;
    private String classRoom;
    private int seatNum;

    void setCourseSelectNum(String input) {
        courseSelectNum = input;
    }

    void setDate(String input) {
        Pattern pattern = Pattern.compile("(\\d\\d\\d\\d-\\d\\d-\\d\\d)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
            String dateInString = matcher.group(1);
            try {
                date = sdf.parse(dateInString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    void setTimePeriod(String input) {
        timePeriod = input;
    }

    void setCourseName(String input) {
        courseName = input;
    }

    void setClassRoom(String input) {
        classRoom = input;
    }

    void setSeatNum(int input) {
        seatNum = input;
    }

    String getCourseSelectNum() {
        return courseSelectNum;
    }

    Date getDate() {
        return date;
    }

    String getTimePeriod() {
        return timePeriod;
    }

    String getCourseName() {
        return courseName;
    }

    String getClassRoom() {
        return classRoom;
    }

    int getSeatNum() {
        return seatNum;
    }
}
