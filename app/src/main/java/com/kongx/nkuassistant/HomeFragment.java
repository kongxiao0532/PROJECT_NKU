package com.kongx.nkuassistant;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HomeFragment extends Fragment implements Connectable{

    private View myView = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }

    @Override
    public void onTaskComplete(Object o, int type) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_home, container, false);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        TextView textView = (TextView)(myView.findViewById(R.id.textView_date));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        textView.setText(dateFormat.format(calendar.getTime()));
        if(year == 2017){
            if(weekOfYear == 1 || weekOfYear == 2){
                textView = (TextView)(myView.findViewById(R.id.textView_week));
                textView.setText("考试");
                textView = (TextView)(myView.findViewById(R.id.textView_period));
                textView.setText("2016-2017 第一学期");
                textView = (TextView)(myView.findViewById(R.id.textView_No_));
                textView.setText(null);
            }
            if(weekOfYear > 2 || weekOfYear <= 6){
                textView = (TextView)(myView.findViewById(R.id.textView_week));
                textView.setText(String.valueOf(weekOfYear-2));
                textView = (TextView)(myView.findViewById(R.id.textView_period));
                textView.setText("寒假");
            }
            if(weekOfYear > 6 || weekOfYear <= 22){
                textView = (TextView)(myView.findViewById(R.id.textView_week));
                textView.setText(String.valueOf(weekOfYear-6));
                textView = (TextView)(myView.findViewById(R.id.textView_period));
                textView.setText("2016-2017 第二学期");
            }
            if(weekOfYear > 22 || weekOfYear <= 24){
                textView = (TextView)(myView.findViewById(R.id.textView_week));
                textView.setText("考试");
                textView = (TextView)(myView.findViewById(R.id.textView_period));
                textView.setText("2016-2017 第二学期");
                textView = (TextView)(myView.findViewById(R.id.textView_No_));
                textView.setText(null);
            }
            if(weekOfYear > 24|| weekOfYear <= 28){
                textView = (TextView)(myView.findViewById(R.id.textView_week));
                textView.setText(String.valueOf(weekOfYear-24));
                textView = (TextView)(myView.findViewById(R.id.textView_period));
                textView.setText("2016-2017 夏季学期");
            }
            if(weekOfYear > 28 || weekOfYear <= 36){
                textView = (TextView)(myView.findViewById(R.id.textView_week));
                textView.setText(String.valueOf(weekOfYear-28));
                textView = (TextView)(myView.findViewById(R.id.textView_period));
                textView.setText("暑假");
            }
        }
        else if(year == 2016){
            if(weekOfYear >= 38 || weekOfYear <= 53){
                textView = (TextView)(myView.findViewById(R.id.textView_week));
                textView.setText(String.valueOf(weekOfYear-37));
                textView = (TextView)(myView.findViewById(R.id.textView_period));
                textView.setText("2016-2017 第一学期");
            }
            if(weekOfYear == 54){
                textView = (TextView)(myView.findViewById(R.id.textView_week));
                textView.setText("考试");
                textView = (TextView)(myView.findViewById(R.id.textView_period));
                textView.setText("2016-2017 第一学期");
                textView = (TextView)(myView.findViewById(R.id.textView_No_));
                textView.setText(null);
            }
        }
        return myView;
    }

}

