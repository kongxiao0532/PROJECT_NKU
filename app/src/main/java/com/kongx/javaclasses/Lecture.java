package com.kongx.javaclasses;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by DELL on 2017/2/22 0022.
 */

public class Lecture {
    private String topic;
    private Date datetime;
    private String dateTimeString;
    private String location;
    private String lecturer;

    public void setDateTime(String input) {
        input = input.replace("&nbsp;", "");
        dateTimeString = input;
        Pattern pattern = Pattern.compile("(\\d\\d\\d\\d)年(\\d.*)月(\\d.*)日.*(\\d\\d)：(\\d\\d)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String dateInString = matcher.group(1) + "-" + matcher.group(2) + "-" + matcher.group(3) + " " + matcher.group(4) + ":" + matcher.group(5);
            try {
                datetime = sdf.parse(dateInString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String input) {
        topic = input;
    }

    public Date getDatetime() {
        return datetime;
    }

    public String getDateTimeString() {
        String tmpString;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        try {
            tmpString = sdf.format(datetime);
        } catch (Exception e) {
            tmpString = dateTimeString;
        }
        return tmpString;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String input) {
        location = input;
    }

    public String getLecturer() {
        return lecturer;
    }

    public void setLecturer(String input) {
        if (input.contains("\n")) input = input.replace("\n", "");
        if (input.contains("\t")) input = input.replace("\t", "");
        while (input.contains("  ")) input = input.replace("  ", " ");
        this.lecturer = input.substring(4, input.length());
    }

}
