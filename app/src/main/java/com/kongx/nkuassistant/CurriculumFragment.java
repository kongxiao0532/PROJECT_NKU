package com.kongx.nkuassistant;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
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
import java.util.IllegalFormatCodePointException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurriculumFragment extends Fragment implements Connectable {
    String[] dayOfWeek = new String[]{"","星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
    String[] startTime = new String[]{"","8:00","8:55","10:00","10:55","12:00","12:55","14:00","14:55","16:00","16:55","18:30","19:25","20:20","21:25"};
    String[] endTime = new String[]{"","8:45","9:40","10:45","11:40","12:45","13:40","14:45","15:40","16:45","17:40","18:30","20:10","21:05","22:00"};
    private View myView = null;
    private int numberOfPages;
    private ListView mlistView;
    private Pattern pattern;
    private Matcher matcher;
   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_curriculum, container, false);
        mlistView = (ListView) myView.findViewById(R.id.list_curriculum);
        Information.selectedCourses.clear();
        new Connect(CurriculumFragment.this,1,null).execute(Information.webUrl+"/xsxk/selectedAction.do");
        return myView;
    }
    void updateUI(){
        mlistView.setAdapter(new MyAdapter(getActivity()));
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
            if(matcher.find())  Information.selectedCourseCount = Integer.parseInt(matcher.group(2));
        }
        pattern = Pattern.compile("(<td align=\"center\" class=\"NavText\">)(.*)(\\r\\n)");
        matcher = pattern.matcher(returnString);
        HashMap<String,String> map = new HashMap<String,String>();
        for(int i = 0; i < (type < numberOfPages ? 12 : (Information.selectedCourseCount - (type - 1) * 12)); i++){
            map = new HashMap<String,String>();
            matcher.find();
            matcher.find();
            map.put("index",matcher.group(2));
            matcher.find();
            matcher.find();
            map.put("name",matcher.group(2));
            matcher.find();
            map.put("dayOfWeek",matcher.group(2));
            matcher.find();
            map.put("startTime",matcher.group(2));
            matcher.find();
            map.put("endTime",matcher.group(2));
            matcher.find();
            map.put("classRoom",matcher.group(2));
            matcher.find();
            map.put("classType",matcher.group(2));
            matcher.find();
            map.put("teacherName",matcher.group(2));
            matcher.find();
            map.put("startWeek",matcher.group(2));
            matcher.find();
            map.put("endWeek",matcher.group(2));
            matcher.find();
            Information.selectedCourses.add(map);
        }
        if(type == numberOfPages)   updateUI();
        else new Connect(CurriculumFragment.this, ++type, "index="+type).execute(Information.webUrl+"/xsxk/selectedPageAction.do");
    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public MyAdapter(Context context) {
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                    convertView = mInflater.inflate(R.layout.selected_list_item,null);
                    holder = new ViewHolder();
                    holder.name = (TextView) convertView.findViewById(R.id.selected_list_name);
                    holder.teacher = (TextView) convertView.findViewById(R.id.selected_list_teacher);
                    holder.index = (TextView) convertView.findViewById(R.id.selected_list_index);
                    holder.time = (TextView) convertView.findViewById(R.id.selected_list_time);
                    convertView.setTag(holder);//绑定ViewHolder对象
                }
            else{
                holder = (ViewHolder)convertView.getTag();//取出ViewHolder对象
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
