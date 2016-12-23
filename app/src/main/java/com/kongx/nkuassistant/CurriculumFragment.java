package com.kongx.nkuassistant;

import android.annotation.SuppressLint;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurriculumFragment extends Fragment implements Connectable,SwipeRefreshLayout.OnRefreshListener {
    private int numberOfPages;
    private SwipeRefreshLayout mRefresh;
    private ListView mlistView;
    private Activity m_activity;
    private ArrayList<HashMap<String,String>> tmpCurriculumList;
    private TextView mNoCurrirulumView;
   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_curriculum, container, false);
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
        new Connect(CurriculumFragment.this,1,null).execute(Information.WEB_URL +"/xsxk/selectedAction.do");
    }

    void update(){
        Calendar calendar = Calendar.getInstance();
        int minute = calendar.get(Calendar.MINUTE);
        String time_now = String.format(Locale.US,"%2d:%2d",calendar.get(Calendar.HOUR_OF_DAY) ,minute);
        Information.curriculum_lastUpdate = Information.date + " " + time_now;
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
            String returnString = new Scanner(is, "GB2312").useDelimiter("\\A").next();
            if (type == 1) {
                pattern = Pattern.compile("(共 )(\\d)( 页,第)");
                matcher = pattern.matcher(returnString);
                if (matcher.find()) numberOfPages = Integer.parseInt(matcher.group(2));
                if (numberOfPages == 0) {
                    mNoCurrirulumView.setVisibility(View.VISIBLE);
                    Information.selectedCourseCount = 0;
                    storeCourses();
                    mRefresh.setRefreshing(false);
                    return;
                }
                mNoCurrirulumView.setVisibility(View.GONE);
                pattern = Pattern.compile("(共 )(.+)( 条记录)");
                matcher = pattern.matcher(returnString);
                if (matcher.find())
                    Information.selectedCourseCount = Integer.parseInt(matcher.group(2));
            }
            pattern = Pattern.compile("(<td align=\"center\" class=\"NavText\">)(.*)(\\r\\n)");
            matcher = pattern.matcher(returnString);
            HashMap<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < (type < numberOfPages ? 12 : (Information.selectedCourseCount - (type - 1) * 12)); i++) {
                map = new HashMap<String, String>();
                matcher.find();
                matcher.find();
                map.put("index", matcher.group(2));
                matcher.find();
                matcher.find();
                map.put("name", matcher.group(2));
                matcher.find();
                map.put("dayOfWeek", matcher.group(2));
                matcher.find();
                map.put("startTime", matcher.group(2));
                matcher.find();
                map.put("endTime", matcher.group(2));
                matcher.find();
                map.put("classRoom", matcher.group(2));
                matcher.find();
                map.put("classType", matcher.group(2));
                matcher.find();
                map.put("teacherName", matcher.group(2));
                matcher.find();
                map.put("startWeek", matcher.group(2));
                matcher.find();
                map.put("endWeek", matcher.group(2));
                matcher.find();
                tmpCurriculumList.add(map);
            }
            if (type == numberOfPages) update();
            else
                new Connect(CurriculumFragment.this, ++type, "index=" + type).execute(Information.WEB_URL + "/xsxk/selectedPageAction.do");
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
            editor.putString("classType" + i, Information.selectedCourses.get(i).get("classType"));
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
        public int getCount() { return Information.selectedCourseCount + 1;}
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
                convertView = mInflater.inflate(R.layout.listview_lastupdate,null);
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
