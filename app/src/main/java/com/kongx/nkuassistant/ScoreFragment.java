package com.kongx.nkuassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class ScoreFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Connector.Callback{
    //数据
    HashMap<Character,Float> averageOfEachType;
    HashMap<Character,Float> creditsSumOfEachType;
    float creditsSum;
    float average_abcd;
    float average_abcde;
    float[] gpaABCDE;
    //控件
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

        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();
        m_activity = getActivity();
        if(Information.studiedCourses == null){
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
        Connector.getInformation(Connector.RequestType.SCORE,ScoreFragment.this,null);
    }

    @Override
    public void onConnectorComplete(Connector.RequestType requestType, Object result) {
        switch (requestType){
            case SCORE:
                Boolean tmpBool = (Boolean) result;
                if(tmpBool){
                    if(m_activity == null) return;
                    //TODO:DECRYPTED
                    Information.studiedCourseCount = Information.studiedCourses.size();
                    if(Information.studiedCourses.size() == 0) return;
                    //记录成绩更新条目数量
                    SharedPreferences settings = m_activity.getSharedPreferences(Information.PREFS_NAME,0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt(Information.Strings.setting_studied_course_count,Information.studiedCourses.size());
                    editor.apply();

                    //评教判断
                    judgeIfEvaluationNeeded();
                    //计算学分绩和GPA
                    calculateAverages();
                    //创建List的显示内容
                    ArrayList<CourseStudied> listToShow = generateListWithDivider();
                    //统计信息显示
                    setAverageOnShow();
                    //停止刷新
                    mRefresh.setRefreshing(false);
                    //用户交互,显示Toast
                    Toast.makeText(m_activity, "已加载"+Information.studiedCourses.size()+"条成绩信息", Toast.LENGTH_SHORT).show();
                    //设置Adapter
                    mScoreList.setAdapter(new ScoreListAdapter(m_activity,listToShow));
                }else {
                    Toast.makeText(m_activity, "成绩加载失败，请重试", Toast.LENGTH_SHORT).show();
                    onRefresh();
                }
                break;
        }
    }

    private void judgeIfEvaluationNeeded(){
        if(Information.studiedCourseAllCount != Information.studiedCourses.size()){
            //本学期有未完成的评教
            //TODO:automatically evaluate
            new AlertDialog.Builder(m_activity).setTitle("本学期评教未完成")
                    .setMessage("本学期评教未完成，部分成绩信息未显示。请到教务网站进行评教操作。")
                    .setPositiveButton("确认", null)
                    .show();
        }
    }

    private ArrayList<CourseStudied> generateListWithDivider(){
        ArrayList<CourseStudied> listToShow = new ArrayList<>();
        //按课程类别排序
        Collections.sort(Information.studiedCourses, new Comparator<CourseStudied>() {
            @Override
            public int compare(CourseStudied o1, CourseStudied o2) {
                return o1.getClassType() - o2.getClassType();
            }
        });
        char nextType = 'A';
        for(CourseStudied tmpCourse : Information.studiedCourses){
            if(tmpCourse.getClassType() == nextType){
                listToShow.add(new CourseStudied(nextType));    //add a divider to the list
                nextType++;
            }
            listToShow.add(tmpCourse);
        }
        return listToShow;
    }

    private void calculateAverages(){
        HashMap<Character,Float> creditsCalculatedSumOfEachType = new HashMap<>();
        //declare vars
        char[] courseType = {'A','B','C','D','E'};
        HashMap<Character,Float>  scoreMulCreSumOfEachType = new HashMap<>();
        averageOfEachType = new HashMap<>();
        creditsSumOfEachType= new HashMap<>();
        gpaABCDE = new float[5];
        float[] gpaMulCreSum = new float[5];
        //initialize
        for(int i = 0;i < 5;i++){
            creditsCalculatedSumOfEachType.put(courseType[i],0f);
            creditsSumOfEachType.put(courseType[i],0f);
            scoreMulCreSumOfEachType.put(courseType[i],0f);
            gpaMulCreSum[i] = 0;
        }
        creditsSum = 0;

        //calculate sum
        for(CourseStudied tmpCourse : Information.studiedCourses){
            if(tmpCourse.score >=60){
                creditsCalculatedSumOfEachType.put(tmpCourse.getClassType(),creditsCalculatedSumOfEachType.get(tmpCourse.getClassType())+tmpCourse.creditCalculated);
                creditsSumOfEachType.put(tmpCourse.getClassType(),creditsSumOfEachType.get(tmpCourse.getClassType())+tmpCourse.credit);
                scoreMulCreSumOfEachType.put(tmpCourse.getClassType(),scoreMulCreSumOfEachType.get(tmpCourse.getClassType())+tmpCourse.score * tmpCourse.creditCalculated);
                for(int i = 0;i < 5;i++){
                    gpaMulCreSum[i] += tmpCourse.getGpas()[i] * tmpCourse.creditCalculated;
                }
            }
        }
        float creditsCalculatedSumABCD =
                creditsCalculatedSumOfEachType.get('A')
                        + creditsCalculatedSumOfEachType.get('B')
                        + creditsCalculatedSumOfEachType.get('C')
                        + creditsCalculatedSumOfEachType.get('D');
        float scoreMulCreSumABCD =
                scoreMulCreSumOfEachType.get('A')
                        + scoreMulCreSumOfEachType.get('B')
                        + scoreMulCreSumOfEachType.get('C')
                        + scoreMulCreSumOfEachType.get('D');
        float creditsSumABCDE = creditsCalculatedSumABCD + creditsCalculatedSumOfEachType.get('E');
        float scoreMulCreSumABCDE = scoreMulCreSumABCD + scoreMulCreSumOfEachType.get('E');
        creditsSum = creditsSumOfEachType.get('A')
                + creditsSumOfEachType.get('B')
                + creditsSumOfEachType.get('C')
                + creditsSumOfEachType.get('D')
                + creditsSumOfEachType.get('E');

        //divide for average
        for (int i = 0;i < 5;i++){
            averageOfEachType.put(courseType[i],creditsCalculatedSumOfEachType.get(courseType[i]) == 0 ? 0 : scoreMulCreSumOfEachType.get(courseType[i]) / creditsCalculatedSumOfEachType.get(courseType[i]));
        }
        average_abcd = creditsCalculatedSumABCD == 0 ? 0 : scoreMulCreSumABCD / creditsCalculatedSumABCD;
        average_abcde = creditsSumABCDE == 0 ? 0 : scoreMulCreSumABCDE / creditsSumABCDE;
        for(int i = 0;i < 5;i++){
            gpaABCDE[i] = creditsSumABCDE == 0 ? 0 : gpaMulCreSum[i] / creditsSumABCDE;
        }
    }

    private void setAverageOnShow(){
        mCreditsAll.setText(String.format(getString(R.string.credits_template),creditsSum));
        mAverageAll.setText(String.format(getString(R.string.average_template),average_abcd,average_abcde));
        mAverageAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Information.studiedCourseCount != -1){
                    showAverageMethod = (++showAverageMethod)%6;
                    switch (showAverageMethod){
                        case 0:
                            mAverageAll.setText(String.format(getString(R.string.average_template),new BigDecimal(average_abcd).setScale(2,BigDecimal.ROUND_HALF_UP),new BigDecimal(average_abcde).setScale(2,BigDecimal.ROUND_HALF_UP)));
                            break;
                        case 1:
                            mAverageAll.setText("标准GPA "+new BigDecimal(gpaABCDE[0]).setScale(3,BigDecimal.ROUND_HALF_UP)+"/4.0");
                            break;
                        case 2:
                            mAverageAll.setText("改进型GPA(1) "+new BigDecimal(gpaABCDE[1]).setScale(3,BigDecimal.ROUND_HALF_UP)+"/4.0");
                            break;
                        case 3:
                            mAverageAll.setText("改进型GPA(2) "+new BigDecimal(gpaABCDE[2]).setScale(3,BigDecimal.ROUND_HALF_UP)+"/4.0");
                            break;
                        case 4:
                            mAverageAll.setText("北大GPA "+new BigDecimal(gpaABCDE[3]).setScale(3,BigDecimal.ROUND_HALF_UP)+"/4.0");
                            break;
                        case 5:
                            mAverageAll.setText("加拿大GPA "+new BigDecimal(gpaABCDE[4]).setScale(3,BigDecimal.ROUND_HALF_UP)+"/4.3");
                            break;
                        default:break;
                    }
                }
            }
        });
    }

    private class ScoreListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private ArrayList<CourseStudied> scoreList;
        ScoreListAdapter(Context context, ArrayList<CourseStudied> listToShow) {
            this.mInflater = LayoutInflater.from(context);
            scoreList = listToShow;
        }
        @Override
        public int getCount() { return scoreList.size();  }

        @Override
        public Object getItem(int position) {
            return scoreList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CourseStudied tmpCourse = scoreList.get(position);
            if(tmpCourse.isDivider){
                //显示一个分隔符
                convertView = mInflater.inflate(R.layout.divider_score_list, null);
                TextView mDividerType = (TextView) convertView.findViewById(R.id.score_list_divider_type);
                TextView mCredits = (TextView) convertView.findViewById(R.id.score_list_divider_credits);
                TextView mAverageScore = (TextView) convertView.findViewById(R.id.score_list_divider_average);
                mDividerType.setText(tmpCourse.getClassType()+"类课");
                mCredits.setText("共" + creditsSumOfEachType.get(tmpCourse.getClassType()) + "学分");
                mAverageScore.setText("学分绩" + averageOfEachType.get(tmpCourse.getClassType()) + "分");
            }else {
                //显示一门课程
                convertView = mInflater.inflate(R.layout.item_score_list, null);
                TextView mName = (TextView) convertView.findViewById(R.id.score_list_item_name);
                TextView mTypeCredit = (TextView) convertView.findViewById(R.id.score_list_item_credit);
                TextView mScore = (TextView) convertView.findViewById(R.id.score_list_item_score);
                mName.setText(tmpCourse.getName());
                mTypeCredit.setText(
                        tmpCourse.getClassType() + "类课  " +
                                tmpCourse.getCredit() + "学分"
                );
                mScore.setText((tmpCourse.creditCalculated == 0) ? "通过" : (!String.valueOf(tmpCourse.getScore()).contains(".")) ? String.valueOf(tmpCourse.getScore()) : String.valueOf(tmpCourse.getScore()).replaceAll("0*$", "").replaceAll("\\.$", ""));
            }
            return convertView;
        }
    }
}


