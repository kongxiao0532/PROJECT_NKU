package com.kongx.nkuassistant;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tk.sunrisefox.httprequest.Connect;
import tk.sunrisefox.httprequest.Request;
import tk.sunrisefox.httprequest.Response;


public class ScoreFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Connect.Callback{
    String lastType;
    private SwipeRefreshLayout mRefresh;
    private ArrayList<CourseStudied> tmpScore;
    private ListView mScoreList;
    private TextView mCreditsAll;
    private TextView mAverageAll;
    private Activity m_activity;
    private int showAverageMethod = 0;
    @Override
        public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Information.resetScores();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_score, container, false);
        mRefresh = (SwipeRefreshLayout) myView.findViewById(R.id.score_refresh);
        mRefresh.setOnRefreshListener(this);
        mScoreList = (ListView) myView.findViewById(R.id.score_list);
        mCreditsAll = (TextView) myView.findViewById(R.id.score_credits);
        mAverageAll = (TextView) myView.findViewById(R.id.score_average);
        mAverageAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Information.studiedCourseCount != -1){
                    showAverageMethod = (++showAverageMethod)%6;
                    switch (showAverageMethod){
                        case 0:
                            mAverageAll.setText("ABCDE百分制学分绩 "+Information.average_abcde+"分");
                            break;
                        case 1:
                            mAverageAll.setText("标准GPA "+Information.gpaABCED[0]+"分");
                            break;
                        case 2:
                            mAverageAll.setText("改进型GPA(1) "+Information.gpaABCED[1]+"分");
                            break;
                        case 3:
                            mAverageAll.setText("改进型GPA(2) "+Information.gpaABCED[2]+"分");
                            break;
                        case 4:
                            mAverageAll.setText("北大GPA "+Information.gpaABCED[3]+"分");
                            break;
                        case 5:
                            mAverageAll.setText("加拿大GPA(满分4.3) "+Information.gpaABCED[4]+"分");
                            break;
                        default:break;
                    }
                }
            }
        });
        onRefresh();
        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();
        m_activity = getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_activity = null;
    }

    public void onRefresh() {
        mRefresh.setRefreshing(true);
        lastType = "A";
        Information.resetScores();
        tmpScore = new ArrayList<>();
        new Request.Builder().url(Information.WEB_URL + Information.Strings.url_score).method("POST").build().send(this);
    }

    @Override
    public void onNetworkComplete(Response response) {
        if(response.code() == 200) {
            String returnString = response.body();
            CourseStudied tmpCourse;
            Pattern pattern;
            Matcher matcher;
            int startPoint = 0;
            pattern = Pattern.compile("</th>\\n(.+)<th>(\\d+)</th>\\n(.+)<th>(.+)</th>");
            matcher = pattern.matcher(returnString);
            if (matcher.find()) {
                Information.studiedCourseCount = Integer.parseInt(matcher.group(2));
                Information.credits_All = Float.parseFloat(matcher.group(4));
            }
            startPoint = matcher.end();
            for (int i = 0; i < Information.studiedCourseCount; i++) {
                tmpCourse = new CourseStudied();
                pattern = Pattern.compile("<td>(.+)</td>");
                matcher = pattern.matcher(returnString);
                if (matcher.find(startPoint)) tmpCourse.setSemester(matcher.group(1));
                try {
                    startPoint = matcher.end();
                }catch (IllegalStateException e) {
                    break;
                }

                pattern = Pattern.compile("<td>(.+)\\t(.+)\\n(.+)</td>");
                matcher = pattern.matcher(returnString);
                if (matcher.find(startPoint)) tmpCourse.name = matcher.group(1);
                startPoint = matcher.end();

                pattern = Pattern.compile("<td>(.+)</td>.+");
                matcher = pattern.matcher(returnString);
                if (matcher.find(startPoint)) tmpCourse.classType = matcher.group(1);

                pattern = Pattern.compile("\\n.+</td>.+<td>(.+)</td>\\n");
                matcher = pattern.matcher(returnString);
                if (matcher.find(startPoint)) tmpCourse.credit = Float.parseFloat(matcher.group(1));
                startPoint = matcher.end();

                //TODO:解决 通过 的情况
                //TODO：解决 双修 的情况
                pattern = Pattern.compile("</td><td style=\"\">.+\\t(.+)\\n");
                matcher = pattern.matcher(returnString);
                if (matcher.find(startPoint)) tmpCourse.setScore(matcher.group(1));
                startPoint = matcher.end();
                tmpScore.add(tmpCourse);
            }
            update();
        }else if (response.code() == 302){
            startActivity(new Intent(m_activity,EduLoginActivity.class));
            m_activity.finish();
        }
    }

    @Override
    public void onNetworkError(Exception exception) {

    }

    private void update(){
        if(m_activity == null) return;
        Information.studiedCourses = tmpScore;
        SharedPreferences settings = m_activity.getSharedPreferences(Information.PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("studiedCourseCount",Information.studiedCourseCount);
        editor.apply();
        float sumABCDE = 0, creditABCDE = 0;
        float[] gpaSumABCDE = new float[]{0,0,0,0,0};
        Information.gpaABCED = new float[5];
        for(CourseStudied tmp : Information.studiedCourses){
            sumABCDE += tmp.score * tmp.creditCalculated;
            for(int i = 0;i < 5;i++){
                gpaSumABCDE[i] += tmp.gpas[i] * tmp.creditCalculated;
            }
            creditABCDE += tmp.creditCalculated;
        }
        for(int i = 0;i < 5;i++){
            Information.gpaABCED[i] = gpaSumABCDE[i] / creditABCDE;
        }
        Information.average_abcde = sumABCDE / creditABCDE;
//        Set<String> keySet = Information.scores.keySet();
//        List<String> keyList = new ArrayList<String>(keySet);
//        Collections.sort(keyList);
//        for (String key : keyList){
//            Information.averages.put(key,Information.scores.get(key) / Information.credits_counted.get(key));
//            Information.credits_All += Information.credits.get(key);
//            Information.credits_All_counted += Information.credits_counted.get(key);
//            Information.scores_All += Information.scores.get(key);
//        }
//        Float A = Information.scores.get("A");
//        Float B = Information.scores.get("B");
//        Float C = Information.scores.get("C");
//        Float D = Information.scores.get("D");
//        Float E = Information.scores.get("E");
//
//        Float cA = Information.credits_counted.get("A");
//        Float cB = Information.credits_counted.get("B");
//        Float cC = Information.credits_counted.get("C");
//        Float cD = Information.credits_counted.get("D");
//        Float cE = Information.credits_counted.get("E");
//
//        Float sumABCD = ((A==null?0:A)+(B==null?0:B)+(C==null?0:C)+(D==null?0:D));
//        Float sumcABCD = ((cA==null?0:cA)+(cB==null?0:cB)+(cC==null?0:cC)+(cD==null?0:cD));
//        Information.average_abcd = sumABCD / sumcABCD;
//        Information.average_abcde = (sumABCD+(E==null?0:E)) / (sumcABCD+(cE==null?0:cE));
//
//        Float FC = Information.scores.get("FC");
//        Float FD = Information.scores.get("FD");
//        Float cFC = Information.credits_counted.get("FC");
//        Float cFD = Information.credits_counted.get("FD");
//        Information.average_f = (((FC==null?0:FC)+(FD==null?0:FD)) / ((cFC==null?0:cFC)+(cFD==null?0:cFD)));
        mCreditsAll.setText(String.format(getString(R.string.credits_template),Information.credits_All));
//        mAverageAll.setText(String.format(getString(R.string.average_template),Information.average_abcd,Information.average_abcde,Information.average_f));
        mAverageAll.setText("ABCDE百分制学分绩"+Information.average_abcde+"分");
        mRefresh.setRefreshing(false);
        mScoreList.setAdapter(new MyAdapter(m_activity));
    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return Information.studiedCourses.size();
        }

        @Override
        public Object getItem(int position) {
            return Information.studiedCourses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
//            boolean isDivider = Information.studiedCourses.get(position).get("name").startsWith("divider");
//                if(isDivider){
//                    convertView = mInflater.inflate(R.layout.score_list_divider,null);
//                    holder = new ViewHolder();
//                    holder.name = (TextView) convertView.findViewById(R.id.score_list_divider_type);
//                    holder.credits = (TextView) convertView.findViewById(R.id.score_list_divider_credits);
//                    holder.score = (TextView) convertView.findViewById(R.id.score_list_divider_average);
//                }
//                else{
                    convertView = mInflater.inflate(R.layout.score_list_item,null);
                    holder = new ViewHolder();
                    holder.name = (TextView) convertView.findViewById(R.id.score_list_item_name);
                    holder.credits = (TextView) convertView.findViewById(R.id.score_list_item_credit);
                    holder.score = (TextView) convertView.findViewById(R.id.score_list_item_score);
//                }
//            if(isDivider){
//                String type = Information.studiedCourses.get(position).get("name").split("\\,")[1];
//                holder.name.setText(type+"类课");
//                holder.credits.setText("共" + Information.credits.get(type) + "学分");
//                holder.score.setText("学分绩" + Information.averages.get(type) + "分");
//            }
//            else{
                holder.name.setText(Information.studiedCourses.get(position).name);
                holder.credits.setText(
                        Information.studiedCourses.get(position).semester+ "  " +
                                Information.studiedCourses.get(position).credit+"学分"
                );
                holder.score.setText((Information.studiedCourses.get(position).creditCalculated == 0) ? "通过" : String.valueOf(Information.studiedCourses.get(position).score));
//            }
            return convertView;
        }
    }
    class ViewHolder{
        TextView name;
        TextView credits;
        TextView score;
    }
}


