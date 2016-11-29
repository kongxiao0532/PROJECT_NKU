package com.kongx.nkuassistant;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ScoreFragment extends Fragment implements Connectable{
    private View myView = null;
    private int numberOfPages;
    private Pattern pattern;
    private Matcher matcher;
    private ListView mScoreList;
    private TextView mCreditsAll;
    private TextView mAverageAll;
    @Override
        public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Information.resetScores();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Information.resetScores();
        myView = inflater.inflate(R.layout.fragment_score, container, false);
        mScoreList = (ListView) myView.findViewById(R.id.score_list);
        mCreditsAll = (TextView) myView.findViewById(R.id.score_credits);
        mAverageAll = (TextView) myView.findViewById(R.id.score_average);
        mScoreList = (ListView) myView.findViewById(R.id.score_list);
        mScoreList.setAdapter(new MyAdapter(getActivity()));
        new Connect(ScoreFragment.this,1,null).execute(Information.webUrl+"/xsxk/studiedAction.do");
        return myView;
    }

    private void updateUI(){
        Information.credits_All = Information.creditsAll_a +
                Information.creditsAll_b +
                Information.creditsAll_c +
                Information.creditsAll_d +
                Information.creditsAll_e;
        Information.average_abcd = (Information.scoresAll_a +
                Information.scoresAll_b +
                Information.scoresAll_c +
                Information.scoresAll_d) / (Information.credits_All - Information.creditsAll_e);
        Information.average_abcde = (Information.scoresAll_a +
                Information.scoresAll_b +
                Information.scoresAll_c +
                Information.scoresAll_d +
                Information.scoresAll_e) / Information.credits_All;
        mCreditsAll.setText(String.format(getString(R.string.credits_template),Information.credits_All));
        mAverageAll.setText(String.format(getString(R.string.average_template),Information.average_abcd,Information.average_abcde));
        if(Information.average_abcd < 80)   mAverageAll.setBackground(getResources().getDrawable(R.drawable.yellow));
        if(Information.average_abcd < 60)   mAverageAll.setBackground(getResources().getDrawable(R.drawable.red));
        for(int i = 0;i<Information.studiedCourseCount;i++){
            Log.e("APP",i+Information.studiedCourses.get(i).get("name").toString());
        }

//        mScoreList.refreshDrawableState();
    }

    @Override
    public void onTaskComplete(Object o, int type) {
        BufferedInputStream is = (BufferedInputStream)o ;
        String returnString = new Scanner(is,"GB2312").useDelimiter("\\A").next();
        if(type == 1){
            pattern = Pattern.compile("(共 )(\\d)( 页,第)");
            matcher = pattern.matcher(returnString);
            if(matcher.find())  numberOfPages = Integer.parseInt(matcher.group(2));
            pattern = Pattern.compile("(共 )(.+)( 条记录)");
            matcher = pattern.matcher(returnString);
            if(matcher.find())  Information.studiedCourseCount = Integer.parseInt(matcher.group(2));
        }
        pattern = Pattern.compile("(<td align=\"center\" class=\"NavText\">)(.+)(\\r)");
        matcher = pattern.matcher(returnString);
        String tmpS1,tmpS2,tmpS3,tmpS4;
        char tmpType;
        HashMap<String,String> map = new HashMap<String,String>();
        for(int i = 0; i < (type < numberOfPages ? 12 : (Information.studiedCourseCount - (type - 1) * 12)); i++){
            map = new HashMap<String,String>();
            matcher.find();
            matcher.find();
            matcher.find();
            map.put("name",matcher.group(2));
            matcher.find();
            tmpS2 = matcher.group(2);//type
            tmpType = tmpS2.charAt(0);
            matcher.find();
            tmpS3 = matcher.group(2);//score
            matcher.find();
            tmpS4 = matcher.group(2);///credit
            map.put("type",tmpS2+"类课");
            map.put("score",tmpS3);
            map.put("credit",tmpS4+"学分");
            map.put("status","good");
            if(tmpS3.charAt(0) >= '0' && tmpS3.charAt(0) <= '9' && !tmpS3.equals("0")){
                if(tmpType == 'A'){
                    Information.scoresAll_a += Float.parseFloat(tmpS3);
                    Information.creditsAll_a += Float.parseFloat(tmpS4);
                }else if(tmpType == 'B'){
                    Information.scoresAll_b += Float.parseFloat(tmpS3);
                    Information.creditsAll_b += Float.parseFloat(tmpS4);
                }else if(tmpType == 'C'){
                    Information.scoresAll_c += Float.parseFloat(tmpS3);
                    Information.creditsAll_c += Float.parseFloat(tmpS4);
                }else if(tmpType == 'D'){
                    Information.scoresAll_d += Float.parseFloat(tmpS3);
                    Information.creditsAll_d += Float.parseFloat(tmpS4);
                }else if(tmpType == 'E'){
                    Information.scoresAll_e += Float.parseFloat(tmpS3);
                    Information.creditsAll_e += Float.parseFloat(tmpS4);
                }
                if(Float.parseFloat(tmpS3) < 80)   map.put("status","pass");
                if(Float.parseFloat(tmpS3) < 60)   map.put("status","failed");
            }
            Information.studiedCourses.add(map);
        }
        if(type == numberOfPages)   updateUI();
        else new Connect(ScoreFragment.this, ++type, "index="+type).execute(Information.webUrl+"/xsxk/studiedPageAction.do");
    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return Information.studiedCourseCount;
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
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.score_list_item,null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.score_list_item_name);
                holder.credits = (TextView) convertView.findViewById(R.id.score_list_item_credit);
                holder.score = (TextView) convertView.findViewById(R.id.score_list_item_score);
                convertView.setTag(holder);//绑定ViewHolder对象
            } else{
                holder = (ViewHolder)convertView.getTag();//取出ViewHolder对象
            }
            /*设置TextView显示的内容，即我们存放在动态数组中的数据*/
//            Log.e("APP",Integer.toString(position));
//            Log.e("APP",Information.studiedCourses.get(position).get("name").toString());
            holder.name.setText(Information.studiedCourses.get(position).get("name").toString());
            holder.credits.setText(Information.studiedCourses.get(position).get("type").toString()+"  "+Information.studiedCourses.get(position).get("credit").toString());
            holder.score.setText(Information.studiedCourses.get(position).get("score").toString());
            return convertView;
//            View view=convertView;
//            if(groupkey.contains(getItem(position))){
//                view=LayoutInflater.from(getContext()).inflate(R.layout.addexam_list_item_tag, null);
//            }else{
//                view=LayoutInflater.from(getContext()).inflate(R.layout.addexam_list_item, null);
//            }
//            TextView text=(TextView) view.findViewById(R.id.addexam_list_item_text);
//            text.setText((CharSequence) getItem(position));
        }

    }
    static class ViewHolder{
        TextView name;
        TextView credits;
        TextView score;
    }
}


