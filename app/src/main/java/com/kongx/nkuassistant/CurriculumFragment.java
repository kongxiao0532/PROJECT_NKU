package com.kongx.nkuassistant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.IDNA;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurriculumFragment extends Fragment implements Connectable,SwipeRefreshLayout.OnRefreshListener {
//    private int numberOfPages;
    private SwipeRefreshLayout mRefresh;
    private ListView mlistView;
    private Activity m_activity;
    private ArrayList<HashMap<String,String>> tmpCurriculumList;
    private TextView mNoCurrirulumView;
    private String stringToPost;
   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_curriculum, container, false);
        stringToPost = String.format(Information.Strings.currriculum_string_template,"31", Information.ids);
        mNoCurrirulumView  = (TextView) myView.findViewById(R.id.textView_noCurriculum);
        mlistView = (ListView) myView.findViewById(R.id.list_curriculum);
        mRefresh = (SwipeRefreshLayout) myView.findViewById(R.id.curriculum_refresh);
        mRefresh.setOnRefreshListener(CurriculumFragment.this);
        mNoCurrirulumView.setVisibility(View.GONE);
        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();
        m_activity = getActivity();
        if(Information.selectedCourseCount == -1){
           onRefresh();
        }else if(Information.selectedCourseCount == 0){
            mNoCurrirulumView.setVisibility(View.VISIBLE);
            mlistView.setVisibility(View.GONE);
        }
        else {
            mNoCurrirulumView.setVisibility(View.GONE);
            mlistView.setVisibility(View.VISIBLE);
            mlistView.setAdapter(new MyAdapter(m_activity));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        m_activity = null;
    }
    public void onRefresh() {
        mRefresh.setRefreshing(true);
        tmpCurriculumList = new ArrayList<>();
        new Connect(CurriculumFragment.this,1, stringToPost).execute(Information.WEB_URL + Information.Strings.url_curriculum);
    }

    void update(){
        Calendar calendar = Calendar.getInstance();
        int minute = calendar.get(Calendar.MINUTE);
        String time_now = String.format(Locale.US,"%2d:%2d",calendar.get(Calendar.HOUR_OF_DAY) ,minute);
        Information.curriculum_lastUpdate = Information.date + " " + time_now;
        Collections.sort(tmpCurriculumList, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> t1, HashMap<String, String> t2) {
                if(Integer.valueOf(t1.get("dayOfWeek")).equals(Integer.valueOf(t2.get("dayOfWeek")))){
                    return Integer.valueOf(t1.get("startTime")) - Integer.valueOf(t2.get("startTime"));
                }else {
                    return Integer.valueOf(t1.get("dayOfWeek")) - Integer.valueOf(t2.get("dayOfWeek"));
                }
            }
        });
        Information.selectedCourses = tmpCurriculumList;
        storeCourses();
        mRefresh.setRefreshing(false);
        mlistView.setAdapter(new MyAdapter(m_activity));
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onTaskComplete(Object o, int type) {
        if(m_activity == null) return;
        if(o == null){
        }else if(o.getClass() == BufferedInputStream.class) {
            BufferedInputStream is = (BufferedInputStream) o;
            Pattern pattern;
            Matcher matcher;
            String returnString = new Scanner(is).useDelimiter("\\A").next();
            HashMap<String, String> map;
            mNoCurrirulumView.setVisibility(View.GONE);
            int startPoint = 0;
            while (true){
                pattern = Pattern.compile("(,name:\")(.+)(\",lab:false\\})");
                matcher = pattern.matcher(returnString);
                if(matcher.find(startPoint)){
                    map = new HashMap<String, String>();
                    startPoint = matcher.end();
                    if (matcher.find(startPoint)){
                        map.put("teacherName",matcher.group(2));
                    }
                    pattern = Pattern.compile("\",\"(.+)\\((\\d+)\\)\",\"\\d+\",\"(.+)\",\"0(\\d+)000000000000000000000000000000000000\"");
                    matcher = pattern.matcher(returnString);
                    if(matcher.find(startPoint)){
                        map.put("name",matcher.group(1));
                        map.put("index",matcher.group(2));
                        map.put("classRoom",matcher.group(3));
                        String tmpString = matcher.group(4);
                        int duration = 0, startWeek = 1;
                        for(int i = 0;i < tmpString.length();i++){
                            if(tmpString.charAt(i) == '1'){
                                if(duration == 0)  startWeek = i + 1;
                                duration++;
                            }
                        }
                        map.put("startWeek",String.valueOf(startWeek));
                        map.put("endWeek",String.valueOf(startWeek + duration - 1));
                    }
                    pattern = Pattern.compile("\\);\\n...index =(\\d+)\\*unitCount\\+(\\d+);");
                    matcher = pattern.matcher(returnString);
                    if(matcher.find(startPoint)){
                        map.put("dayOfWeek",String.valueOf(Integer.parseInt(matcher.group(1)) + 1));
                        map.put("startTime",String.valueOf(Integer.parseInt(matcher.group(2)) + 1));
                    }
                    pattern = Pattern.compile("index =(\\d+)\\*unitCount\\+(\\d+);\\n(.+)\\n...[^i]");
                    matcher = pattern.matcher(returnString);
                    if(matcher.find(startPoint)){
                        map.put("endTime",String.valueOf(Integer.parseInt(matcher.group(2)) + 1));
                        startPoint = matcher.end();
                    }
                    tmpCurriculumList.add(map);
                }else {
                    break;
                }
            }
            Information.selectedCourseCount = tmpCurriculumList.size();
            update();
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

    boolean storeCourses() {
        SharedPreferences settings = m_activity.getSharedPreferences(Information.COURSE_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("selectedCourseCount", Information.selectedCourseCount);
        for (int i = 0; i < Information.selectedCourseCount; i++) {
            editor.putString("index" + i, Information.selectedCourses.get(i).get("index"));
            editor.putString("name" + i, Information.selectedCourses.get(i).get("name"));
            editor.putString("dayOfWeek" + i, Information.selectedCourses.get(i).get("dayOfWeek"));
            editor.putString("startTime" + i, Information.selectedCourses.get(i).get("startTime"));
            editor.putString("endTime" + i, Information.selectedCourses.get(i).get("endTime"));
            editor.putString("classRoom" + i, Information.selectedCourses.get(i).get("classRoom"));
//            editor.putString("classType" + i, Information.selectedCourses.get(i).get("classType"));
            editor.putString("teacherName" + i, Information.selectedCourses.get(i).get("teacherName"));
            editor.putString("startWeek" + i, Information.selectedCourses.get(i).get("startWeek"));
            editor.putString("endWeek" + i, Information.selectedCourses.get(i).get("endWeek"));
        }
        editor.putString("curriculum_lastUpdate",Information.curriculum_lastUpdate);
        return editor.commit();
    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {  return Information.selectedCourseCount + 1;}
        @Override
        public Object getItem(int position) {
            return Information.selectedCourses.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup parent) {
            if(position == Information.selectedCourseCount){
                convertView = mInflater.inflate(R.layout.listview_lastupdate, null);
                TextView last_update_view = (TextView) convertView.findViewById(R.id.last_update);
                last_update_view.setText("最后更新：" + ((Information.curriculum_lastUpdate == null) ? "从未更新" : Information.curriculum_lastUpdate));
            }
            else {
                ViewHolder holder;
//                if(convertView == null){
                    convertView = mInflater.inflate(R.layout.selected_list_item, null);
                    holder = new ViewHolder();
                    holder.name = (TextView) convertView.findViewById(R.id.selected_list_name);
                    holder.day = (TextView) convertView.findViewById(R.id.selected_list_day);
                    holder.index = (TextView) convertView.findViewById(R.id.selected_list_index);
                    holder.time = (TextView) convertView.findViewById(R.id.selected_list_time);
//                    convertView.setTag(holder);
//                }
//                else{
//                    holder = (ViewHolder)convertView.getTag();
//                }
                holder.name.setText(Information.selectedCourses.get(position).get("name"));
                holder.day.setText(Information.dayOfWeek[Integer.parseInt(Information.selectedCourses.get(position).get("dayOfWeek"))]);
                holder.index.setText(Information.selectedCourses.get(position).get("index"));
                holder.time.setText(Information.startTime[Integer.parseInt(Information.selectedCourses.get(position).get("startTime"))]+"-"+
                        Information.endTime[Integer.parseInt(Information.selectedCourses.get(position).get("endTime"))]);
            }
            return convertView;
        }

    }

    class ViewHolder{
        TextView name;
        TextView day;
        TextView index;
        TextView time;
    }
}
