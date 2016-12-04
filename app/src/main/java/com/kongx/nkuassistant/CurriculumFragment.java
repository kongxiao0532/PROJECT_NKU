package com.kongx.nkuassistant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class CurriculumFragment extends Fragment implements Connectable {
    public static final String[] dayOfWeek = new String[]{"","星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
    private static final String[] startTime = new String[]{"","8:00","8:55","10:00","10:55","12:00","12:55","14:00","14:55","16:00","16:55","18:30","19:25","20:20","21:25"};
    private static final String[] endTime = new String[]{"","8:45","9:40","10:45","11:40","12:45","13:40","14:45","15:40","16:45","17:40","18:30","20:10","21:05","22:00"};
    private int numberOfPages;
    private ListView mlistView;
    private Activity m_activity;
   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Information.selectedCourses.clear();
        View myView = inflater.inflate(R.layout.fragment_curriculum, container, false);
        mlistView = (ListView) myView.findViewById(R.id.list_curriculum);
        return myView;
    }

    @Override
    public void onResume() {
        m_activity = getActivity();
        new Connect(CurriculumFragment.this,1,null).execute(Information.webUrl+"/xsxk/selectedAction.do");
    }

    @Override
    public void onPause() {
        m_activity = null;
    }

    void updateUI(){
        storeCourses();
        mlistView.setAdapter(new MyAdapter(m_activity));
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onTaskComplete(Object o, int type) {
        if(m_activity == null) return;
        if(o == null){
            Log.e("APP", "What the fuck?");
        }else if(o.getClass() == BufferedInputStream.class) {

            BufferedInputStream is = (BufferedInputStream) o;
            Pattern pattern;
            Matcher matcher;
            String returnString = new Scanner(is, "GB2312").useDelimiter("\\A").next();
            Log.e("ASS", returnString);
            if (type == 1) {
                pattern = Pattern.compile("(共 )(\\d)( 页,第)");
                matcher = pattern.matcher(returnString);
                if (matcher.find()) numberOfPages = Integer.parseInt(matcher.group(2));
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
                Information.selectedCourses.add(map);
            }
            if (type == numberOfPages) updateUI();
            else
                new Connect(CurriculumFragment.this, ++type, "index=" + type).execute(Information.webUrl + "/xsxk/selectedPageAction.do");
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

    private boolean storeCourses() {
        SharedPreferences settings = m_activity.getSharedPreferences(Information.COURSE_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("selectedCourseCount", String.valueOf(Information.selectedCourseCount));
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
        return editor.commit();
    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return Information.selectedCourseCount;
        }

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
            ViewHolder holder;
            if(convertView == null){
                    convertView = mInflater.inflate(R.layout.selected_list_item, null);
                    holder = new ViewHolder();
                    holder.name = (TextView) convertView.findViewById(R.id.selected_list_name);
                    holder.teacher = (TextView) convertView.findViewById(R.id.selected_list_teacher);
                    holder.index = (TextView) convertView.findViewById(R.id.selected_list_index);
                    holder.time = (TextView) convertView.findViewById(R.id.selected_list_time);
                    convertView.setTag(holder);
                }
            else{
                holder = (ViewHolder)convertView.getTag();
            }
                holder.name.setText(Information.selectedCourses.get(position).get("name"));
                holder.teacher.setText(Information.selectedCourses.get(position).get("teacherName")+" （"+Information.selectedCourses.get(position).get("classType")+"）");
                holder.index.setText(Information.selectedCourses.get(position).get("classRoom"));
                holder.time.setText(dayOfWeek[Integer.parseInt(Information.selectedCourses.get(position).get("dayOfWeek"))]+" "+
                        startTime[Integer.parseInt(Information.selectedCourses.get(position).get("startTime"))]+"-"+
                        endTime[Integer.parseInt(Information.selectedCourses.get(position).get("endTime"))]);
            return convertView;
        }

    }

    class ViewHolder{
        TextView name;
        TextView teacher;
        TextView index;
        TextView time;
    }
}
