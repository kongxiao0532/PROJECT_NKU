package com.kongx.nkuassistant;

/**
 * Created by DELL on 2017/2/17 0017.
 */

public class CourseStudied {
    String semester;
    String name;
    String classType;
    String classId;
    float score;
    float credit;
    float creditCalculated;             //防止有“通过”的情况
    float[] gpas;
    public void setSemester(String semester){
        try {
            this.semester = semester.substring(0,9)+"年第"+semester.charAt(semester.length() - 1) + "学期";
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    void setScore(String score){
        try {
            if(score.charAt(0) >= '0' && score.charAt(0) <= '9'){       //课程有分数
                creditCalculated = credit;
                this.score = Float.parseFloat(score);
                calculateGPA();
            }else{              //课程为 通过
                this.score = creditCalculated = 0;
                gpas = new float[5];
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void calculateGPA(){
        gpas = new float[5];
        //Standart GPA
        if(score >= 90) gpas[0] = 4.0f;
        else if(score >= 80)    gpas[0] = 3.0f;
        else if(score >= 70)    gpas[0] = 2.0f;
        else if(score >= 60)    gpas[0] = 1.0f;
        else gpas[0] = 0f;
        //Modified GPA(1)
        if(score >= 85) gpas[1] = 4.0f;
        else if(score >= 70)    gpas[1] = 3.0f;
        else if(score >= 60)    gpas[1] = 2.0f;
        else gpas[1] = 0f;
        //Modified GPA(2)
        if(score >= 85) gpas[2] = 4.0f;
        else if(score >= 75)    gpas[2] = 3.0f;
        else if(score >= 60)    gpas[2] = 2.0f;
        else gpas[2] = 0f;
        //PKU GPA
        if(score >= 90) gpas[3] = 4.0f;
        else if(score >= 85)    gpas[3] = 3.7f;
        else if(score >= 82)    gpas[3] = 3.3f;
        else if(score >= 78)    gpas[3] = 3.0f;
        else if(score >= 75)    gpas[3] = 2.7f;
        else if(score >= 72)    gpas[3] = 2.3f;
        else if(score >= 68)    gpas[3] = 2.0f;
        else if(score >= 64)    gpas[3] = 1.5f;
        else if(score >= 60)    gpas[3] = 1.0f;
        else gpas[3] = 0f;
        //CANADIAN GPA
        if(score >= 90) gpas[4] = 4.3f;
        else if(score >= 85)    gpas[4] = 4.0f;
        else if(score >= 80)    gpas[4] = 3.7f;
        else if(score >= 75)    gpas[4] = 3.3f;
        else if(score >= 70)    gpas[4] = 3.0f;
        else if(score >= 65)    gpas[4] = 2.7f;
        else if(score >= 60)    gpas[4] = 2.3f;
        else gpas[4] = 0f;
    }
}
