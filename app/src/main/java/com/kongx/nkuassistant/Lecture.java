package com.kongx.nkuassistant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by DELL on 2017/2/22 0022.
 */

public class Lecture {
    String topic;
    String time;
    int year;
    int month;
    int day;
    String location;
    String lecturer;
    void setTime(String input){
        input = input.replace("&nbsp;"," ");
        this.time = input.substring(3,input.length());
        Pattern pattern = Pattern.compile("(\\d\\d\\d\\d)年(\\d.*)月(\\d.*)日");
        Matcher matcher = pattern.matcher(input);
        if(matcher.find()){
            this.year = Integer.parseInt(matcher.group(1));
            this.month = Integer.parseInt(matcher.group(2));
            this.day = Integer.parseInt(matcher.group(3));
        }
    }
    void setLecturer(String input){
        if(input.contains("\n"))  input = input.replace("\n","");
        if(input.contains("\t"))  input = input.replace("\t","");
        while(input.contains("  ")) input = input.replace("  "," ");
        this.lecturer = input.substring(4,input.length());
    }
}
