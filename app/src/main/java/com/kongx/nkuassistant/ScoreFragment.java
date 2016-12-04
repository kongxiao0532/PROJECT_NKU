package com.kongx.nkuassistant;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ScoreFragment extends Fragment implements Connectable{
    char lastType;
    private View myView = null;
    private int numberOfPages;
    private Pattern pattern;
    private Matcher matcher;
    private ListView mScoreList;
    private TextView mCreditsAll;
    private TextView mAverageAll;
    private Activity m_activity;
    @Override
        public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Information.resetScores();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Information.resetScores();
        Information.studiedCourses.clear();
        lastType = 'A';
        myView = inflater.inflate(R.layout.fragment_score, container, false);
        mScoreList = (ListView) myView.findViewById(R.id.score_list);
        mCreditsAll = (TextView) myView.findViewById(R.id.score_credits);
        mAverageAll = (TextView) myView.findViewById(R.id.score_average);
        return myView;
    }

    @Override
    public void onResume() {
        m_activity = getActivity();
        new Connect(ScoreFragment.this, 1, null).execute(Information.webUrl + "/xsxk/selectedAction.do");
    }

    @Override
    public void onPause() {
        m_activity = null;
    }

    private void updateUI(){
        SharedPreferences settings = m_activity.getSharedPreferences(Information.PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("studiedCourseCount",String.valueOf(Information.studiedCourseCount));
        editor.apply();
        for(int i = 0;i < 5;i++){
            Information.averages[i] = Information.scores[i] / Information.credits[i];
            Information.credits_All += Information.credits[i];
            Information.scores_All += Information.scores[i];
            if(i == 3){
                Information.average_abcd = Information.scores_All / Information.credits_All;
            }
        }
        Information.average_abcde = Information.scores_All / Information.credits_All;
        mCreditsAll.setText(String.format(getString(R.string.credits_template),Information.credits_All));
        mAverageAll.setText(String.format(getString(R.string.average_template),Information.average_abcd,Information.average_abcde));
        if(Information.average_abcd < 80)   mAverageAll.setBackground(getResources().getDrawable(R.drawable.yellow));
        if(Information.average_abcd < 60)   mAverageAll.setBackground(getResources().getDrawable(R.drawable.red));
        mScoreList.setAdapter(new MyAdapter(m_activity));
    }

    @Override
    public void onTaskComplete(Object o, int type) {
        if(m_activity == null)return;
        if(o.getClass() == BufferedInputStream.class) {
            BufferedInputStream is = (BufferedInputStream) o;
            String returnString = new Scanner(is, "GB2312").useDelimiter("\\A").next();
            HashMap<String, String> map = new HashMap<String, String>();
            if (type == 1) {
                pattern = Pattern.compile("(共 )(\\d)( 页,第)");
                matcher = pattern.matcher(returnString);
                if (matcher.find()) numberOfPages = Integer.parseInt(matcher.group(2));
                pattern = Pattern.compile("(共 )(.+)( 条记录)");
                matcher = pattern.matcher(returnString);
                if (matcher.find())
                    Information.studiedCourseCount = Integer.parseInt(matcher.group(2));
                map.put("name", "dividerA");
                Information.studiedCourses.add(map);
            }
            pattern = Pattern.compile("(<td align=\"center\" class=\"NavText\">)(.*)(\\r)");
            matcher = pattern.matcher(returnString);
            String tmpS1, tmpS2, tmpS3, tmpS4;
            for (int i = 0; i < (type < numberOfPages ? 12 : (Information.studiedCourseCount - (type - 1) * 12)); i++) {
                map = new HashMap<>();
                matcher.find();
                matcher.find();
                matcher.find();
                tmpS1 = matcher.group(2);//name
                matcher.find();
                tmpS2 = matcher.group(2);//type
                matcher.find();
                tmpS3 = matcher.group(2);//score
                matcher.find();
                tmpS4 = matcher.group(2);///credit
                matcher.find();
                matcher.find();
                map.put("name", tmpS1);
                map.put("type", tmpS2 + "类课");
                map.put("score", tmpS3);
                map.put("credit", tmpS4 + "学分");
                map.put("status", "good");
                if (tmpS2.charAt(0) != lastType) {
                    HashMap<String, String> divider = new HashMap<>();
                    lastType = tmpS2.charAt(0);
                    divider.put("name", "divider" + lastType);
                    Information.studiedCourses.add(divider);
                }
                if (tmpS3.charAt(0) >= '0' && tmpS3.charAt(0) <= '9' && !tmpS3.equals("0")) {

                    Information.scores[tmpS2.charAt(0) - 'A'] += Float.parseFloat(tmpS3) * Float.parseFloat(tmpS4);
                    Information.credits[tmpS2.charAt(0) - 'A'] += Float.parseFloat(tmpS4);
                    if (Float.parseFloat(tmpS3) < 80) map.put("status", "pass");
                    if (Float.parseFloat(tmpS3) < 60) map.put("status", "failed");
                }
                Information.studiedCourses.add(map);
            }
            if (type == numberOfPages) updateUI();
            else
                new Connect(ScoreFragment.this, ++type, "index=" + type).execute(Information.webUrl + "/xsxk/studiedPageAction.do");
        }else if(o.getClass() == Integer.class){
            Integer code = (Integer)o;
            if(code == 302){
                this.startActivity(new Intent(m_activity,EduLoginActivity.class));
                m_activity.finish();
            }
        }else if(o.getClass() == SocketTimeoutException.class){
            Log.e("APP","SocketTimeoutException!");
        }
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
            boolean isDivider = Information.studiedCourses.get(position).get("name").startsWith("divider");
//            if(convertView == null){
                if(isDivider){
                    convertView = mInflater.inflate(R.layout.score_list_divider,null);
                    holder = new ViewHolder();
                    holder.name = (TextView) convertView.findViewById(R.id.score_list_divider_type);
                    holder.credits = (TextView) convertView.findViewById(R.id.score_list_divider_credits);
                    holder.score = (TextView) convertView.findViewById(R.id.score_list_divider_average);
//                    convertView.setTag(holder);//绑定ViewHolder对象
                }
                else{
                    convertView = mInflater.inflate(R.layout.score_list_item,null);
                    holder = new ViewHolder();
                    holder.name = (TextView) convertView.findViewById(R.id.score_list_item_name);
                    holder.credits = (TextView) convertView.findViewById(R.id.score_list_item_credit);
                    holder.score = (TextView) convertView.findViewById(R.id.score_list_item_score);
//                    convertView.setTag(holder);//绑定ViewHolder对象
                }
//            } else{
//                holder = (ViewHolder)convertView.getTag();//取出ViewHolder对象
//            }
            if(isDivider){
                char type = Information.studiedCourses.get(position).get("name").charAt(7);
                holder.name.setText(type+"类课");
                holder.credits.setText("共" + Information.credits[type - 'A'] + "学分");
                holder.score.setText("学分绩" + Information.averages[type - 'A'] + "分");
            }
            else{
                holder.name.setText(Information.studiedCourses.get(position).get("name"));
                holder.credits.setText(Information.studiedCourses.get(position).get("type")+"  "+Information.studiedCourses.get(position).get("credit"));
                holder.score.setText(Information.studiedCourses.get(position).get("score"));
            }
            return convertView;
        }

    }
    class ViewHolder{
        TextView name;
        TextView credits;
        TextView score;
    }
}


