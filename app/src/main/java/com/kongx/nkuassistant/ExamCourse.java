package com.kongx.nkuassistant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kongx on 2017/6/28 0028.
 */

public class ExamCourse {
    private String courseSelectNum;
    private Date date;
    private String timePeriod;
    private String examType;
    private String courseName;
    private String classRoom;
    private String seatNum;

    String getCourseSelectNum() {
        return courseSelectNum;
    }

    void setCourseSelectNum(String input) {
        courseSelectNum = input;
    }

    Date getDate() {
        return date;
    }

    void setDate(String input) {
        Pattern pattern = Pattern.compile("(\\d\\d\\d\\d-\\d\\d-\\d\\d)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateInString = matcher.group(1);
            try {
                date = sdf.parse(dateInString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    String getDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    String getTimePeriod() {
        return timePeriod;
    }

    void setTimePeriod(String input) {
        timePeriod = input;
    }

    String getCourseName() {
        return courseName;
    }

    void setCourseName(String input) {
        courseName = input;
    }

    String getClassRoom() {
        return classRoom;
    }

    void setClassRoom(String input) {
        classRoom = input;
    }

    String getExamType() {
        return examType;
    }

    void setExamType(String input) {
        examType = input;
    }

    String getSeatNum() {
        return seatNum;
    }

    void setSeatNum(String input) {
        seatNum = input;
    }

    public static class ExamComparator implements Comparator<ExamCourse> {

        @Override
        public int compare(ExamCourse e1, ExamCourse e2) {
            int month1 = 0, month2 = 0, day1 = 0, day2 = 0, hour1 = 0, hour2 = 0;
            Calendar cal = Calendar.getInstance();
            cal.setTime(e1.getDate());
            month1 = cal.get(Calendar.MONTH);
            day1 = cal.get(Calendar.DAY_OF_MONTH);
            cal.setTime(e2.getDate());
            month2 = cal.get(Calendar.MONTH);
            day2 = cal.get(Calendar.DAY_OF_MONTH);

            Pattern pattern = Pattern.compile("(\\d\\d):\\d\\d~\\d\\d:\\d\\d");
            Matcher matcher = pattern.matcher(e1.getTimePeriod());
            if (matcher.find()) {
                hour1 = Integer.parseInt(matcher.group(1));
            }
            matcher = pattern.matcher(e2.getTimePeriod());
            if (matcher.find()) {
                hour2 = Integer.parseInt(matcher.group(1));
            }
            if (month1 == month2) {
                if (day1 == day2) {
                    if (hour1 == hour2) {
                        return 0;
                    } else return hour1 - hour2;
                } else return day1 - day2;
            } else return month1 - month2;
        }
    }
}
