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
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
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


public class ScoreFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Connector.Callback{
    String lastType;
    private SwipeRefreshLayout mRefresh;
    private ListView mScoreList;
    private TextView mCreditsAll;
    private TextView mAverageAll;
    private Activity m_activity;
    private int showAverageMethod = 0;
    @Override
        public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                            mAverageAll.setText("ABCDE百分制学分绩 "+new BigDecimal(Information.average_abcde).setScale(3,BigDecimal.ROUND_HALF_UP)+"/100");
                            break;
                        case 1:
                            mAverageAll.setText("标准GPA "+new BigDecimal(Information.gpaABCED[0]).setScale(3,BigDecimal.ROUND_HALF_UP)+"/4.0");
                            break;
                        case 2:
                            mAverageAll.setText("改进型GPA(1) "+new BigDecimal(Information.gpaABCED[1]).setScale(3,BigDecimal.ROUND_HALF_UP)+"/4.0");
                            break;
                        case 3:
                            mAverageAll.setText("改进型GPA(2) "+new BigDecimal(Information.gpaABCED[2]).setScale(3,BigDecimal.ROUND_HALF_UP)+"/4.0");
                            break;
                        case 4:
                            mAverageAll.setText("北大GPA "+new BigDecimal(Information.gpaABCED[3]).setScale(3,BigDecimal.ROUND_HALF_UP)+"/4.0");
                            break;
                        case 5:
                            mAverageAll.setText("加拿大GPA "+new BigDecimal(Information.gpaABCED[4]).setScale(3,BigDecimal.ROUND_HALF_UP)+"/4.3");
                            break;
                        default:break;
                    }
                }
            }
        });
        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();
        m_activity = getActivity();
        if(Connector.tmpStudiedCourseCount == -1){
            onRefresh();
        }else onConnectorComplete(Connector.RequestType.SCORE,true);
    }

    @Override
    public void onPause() {
        super.onPause();
        m_activity = null;
    }

    public void onRefresh() {
        mRefresh.setRefreshing(true);
//        lastType = "A";
        Information.resetScores();
        Connector.getInformation(Connector.RequestType.SCORE,ScoreFragment.this,null);
    }

    @Override
    public void onConnectorComplete(Connector.RequestType requestType, Object result) {
        switch (requestType){
            case SCORE:
                Boolean tmpBool = (Boolean) result;
                if(tmpBool){
                    if(m_activity == null) return;
                    Information.studiedCourseCount = Information.studiedCourses.size();
                    if(Information.studiedCourseCount == 0) return;
                    Toast.makeText(getActivity(), "已加载"+Information.studiedCourseCount+"条成绩信息", Toast.LENGTH_SHORT).show();
                    SharedPreferences settings = m_activity.getSharedPreferences(Information.PREFS_NAME,0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt(Information.Strings.setting_studied_course_count,Information.studiedCourseCount);
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
                    mAverageAll.setText("ABCDE百分制学分绩"+new BigDecimal(Information.average_abcde).setScale(3,BigDecimal.ROUND_HALF_UP)+"分");
                    mRefresh.setRefreshing(false);
                    mScoreList.setAdapter(new MyAdapter(m_activity));
                }else {

                }
                break;
            case LOGIN:
                Log.e("SCORE",result.toString());
                if(result.getClass() == Boolean.class && (Boolean)result) {                //Login Successfully
                    Toast.makeText(getActivity(), "已重新登录", Toast.LENGTH_SHORT).show();
                    onRefresh();
                }else {
                    Toast.makeText(getActivity(), "重新登录失败", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(m_activity,EduLoginActivity.class));
                    m_activity.finish();
                }
                break;
            default:
                break;
        }
    }


    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return Information.studiedCourseCount == -1 ? 0 : Information.studiedCourseCount;
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
                holder.score.setText((Information.studiedCourses.get(position).creditCalculated == 0) ? "通过" : (!String.valueOf(Information.studiedCourses.get(position).score).contains(".")) ? String.valueOf(Information.studiedCourses.get(position).score) : String.valueOf(Information.studiedCourses.get(position).score).replaceAll("0*$", "").replaceAll("\\.$", ""));
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


