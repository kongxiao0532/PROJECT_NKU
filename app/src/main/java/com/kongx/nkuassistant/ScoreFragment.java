package com.kongx.nkuassistant;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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


public class ScoreFragment extends Fragment implements Connectable, SwipeRefreshLayout.OnRefreshListener{
    String lastType;
    private int numberOfPages;
    private SwipeRefreshLayout mRefresh;
    private ArrayList<HashMap<String,String>> tmpScore;
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
        onRefresh();
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
        new Connect(this,1,null).execute(Information.WEB_URL +"/xsxk/studiedAction.do");
    }

    private void update(){
        if(m_activity == null) return;
        Information.studiedCourses = tmpScore;
        SharedPreferences settings = m_activity.getSharedPreferences(Information.PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("studiedCourseCount",Information.studiedCourseCount);
        editor.apply();

        Set<String> keySet = Information.scores.keySet();
        List<String> keyList = new ArrayList<String>(keySet);
        Collections.sort(keyList);
        for (String key : keyList){
            Information.averages.put(key,Information.scores.get(key) / Information.credits_counted.get(key));
            Information.credits_All += Information.credits.get(key);
            Information.credits_All_counted += Information.credits_counted.get(key);
            Information.scores_All += Information.scores.get(key);
        }
        Float A = Information.scores.get("A");
        Float B = Information.scores.get("B");
        Float C = Information.scores.get("C");
        Float D = Information.scores.get("D");
        Float E = Information.scores.get("E");

        Float cA = Information.credits_counted.get("A");
        Float cB = Information.credits_counted.get("B");
        Float cC = Information.credits_counted.get("C");
        Float cD = Information.credits_counted.get("D");
        Float cE = Information.credits_counted.get("E");

        Float sumABCD = ((A==null?0:A)+(B==null?0:B)+(C==null?0:C)+(D==null?0:D));
        Float sumcABCD = ((cA==null?0:cA)+(cB==null?0:cB)+(cC==null?0:cC)+(cD==null?0:cD));
        Information.average_abcd = sumABCD / sumcABCD;
        Information.average_abcde = (sumABCD+(E==null?0:E)) / (sumcABCD+(cE==null?0:cE));

        Float FC = Information.scores.get("FC");
        Float FD = Information.scores.get("FD");
        Float cFC = Information.credits_counted.get("FC");
        Float cFD = Information.credits_counted.get("FD");
        Information.average_f = (((FC==null?0:FC)+(FD==null?0:FD)) / ((cFC==null?0:cFC)+(cFD==null?0:cFD)));
        mCreditsAll.setText(String.format(getString(R.string.credits_template),Information.credits_All));
        mAverageAll.setText(String.format(getString(R.string.average_template),Information.average_abcd,Information.average_abcde,Information.average_f));
        mRefresh.setRefreshing(false);
        mScoreList.setAdapter(new MyAdapter(m_activity));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onTaskComplete(Object o, int type) {
        if(m_activity == null)return;
        if(o.getClass() == BufferedInputStream.class) {
            BufferedInputStream is = (BufferedInputStream) o;
            String returnString = new Scanner(is, "GB2312").useDelimiter("\\A").next();
            HashMap<String, String> map = new HashMap<String, String>();
            Pattern pattern;
            Matcher matcher;
            if (type == 1) {
                pattern = Pattern.compile("(共 )(\\d)( 页,第)");
                matcher = pattern.matcher(returnString);
                if (matcher.find()) numberOfPages = Integer.parseInt(matcher.group(2));
                pattern = Pattern.compile("(共 )(.+)( 条记录)");
                matcher = pattern.matcher(returnString);
                if (matcher.find())
                    Information.studiedCourseCount = Integer.parseInt(matcher.group(2));
                map.put("name", "divider,A");
                tmpScore.add(map);
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
                if (!tmpS2.equals(lastType)) {
                    HashMap<String, String> divider = new HashMap<>();
                    lastType = tmpS2;
                    divider.put("name", "divider," + lastType);
                    tmpScore.add(divider);
                }
                Information.credits.put(tmpS2,(Information.credits.get(tmpS2)==null?0:Information.credits.get(tmpS2))+Float.parseFloat(tmpS4));
                if (tmpS3.charAt(0) >= '0' && tmpS3.charAt(0) <= '9' && !tmpS3.equals("0")) {
                    Information.scores.put(
                            tmpS2
                            , (Information.scores.get(tmpS2)==null?0:Information.scores.get(tmpS2))+Float.parseFloat(tmpS3) * Float.parseFloat(tmpS4)
                    );
                    Information.credits_counted.put(
                            tmpS2,(Information.credits_counted.get(tmpS2)==null?0:Information.credits_counted.get(tmpS2))+Float.parseFloat(tmpS4)
                    );
                    if (Float.parseFloat(tmpS3) < 80) map.put("status", "pass");
                    if (Float.parseFloat(tmpS3) < 60) map.put("status", "failed");
                }else{
                    Information.scores.put(
                            tmpS2
                            , (Information.scores.get(tmpS2)==null?0:Information.scores.get(tmpS2))+0
                    );
                    Information.credits_counted.put(
                            tmpS2,(Information.credits_counted.get(tmpS2)==null?0:Information.credits_counted.get(tmpS2))+0
                    );
                }
                tmpScore.add(map);
            }
            if (type == numberOfPages) update();
            else new Connect(ScoreFragment.this, ++type, "index=" + type).execute(Information.WEB_URL + "/xsxk/studiedPageAction.do");
        }else if(o.getClass() == Integer.class){
            Integer code = (Integer)o;
            if(code == 302){
                this.startActivity(new Intent(m_activity,EduLoginActivity.class));
                m_activity.finish();
            }
        }else if(o.getClass() == SocketTimeoutException.class){
            Log.e("ScoreFragment","SocketTimeoutException!");
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
                String type = Information.studiedCourses.get(position).get("name").split("\\,")[1];
                holder.name.setText(type+"类课");
                holder.credits.setText("共" + Information.credits.get(type) + "学分");
                holder.score.setText("学分绩" + Information.averages.get(type) + "分");
            }
            else{
                holder.name.setText(Information.studiedCourses.get(position).get("name"));
                holder.credits.setText(
                        Information.studiedCourses.get(position).get("type")+"  "+Information.studiedCourses.get(position).get("credit")
                );
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


