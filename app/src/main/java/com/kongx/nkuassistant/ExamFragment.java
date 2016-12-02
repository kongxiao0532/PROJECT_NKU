package com.kongx.nkuassistant;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExamFragment extends Fragment implements Connectable{
    private View myView = null;
    private int numberOfPages;
    private ListView mExamList;
    private TextView mNoText;
    private Pattern pattern;
    private Matcher matcher;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_exam, container, false);
        Information.exams.clear();
        mExamList = (ListView) myView.findViewById(R.id.exam_list);
        mNoText = (TextView) myView.findViewById(R.id.textView_noExam);
        new Connect(ExamFragment.this,0,null).execute(Information.webUrl+"/xxcx/stdexamarrange/listAction.do");
        return myView;
    }

    private void updateUI(){

        mExamList.setAdapter(new MyAdapter(getActivity()));
    }

    @Override
    public void onTaskComplete(Object o, int type) {
        if(o == null){
            Log.e("APP", "What the fuck?");
        }else if(o.getClass() == BufferedInputStream.class) {
            BufferedInputStream is = (BufferedInputStream)o ;
            String returnString = new Scanner(is,"GB2312").useDelimiter("\\A").next();
            pattern = Pattern.compile("<strong>(.+)(<\\/strong>)");
            matcher = pattern.matcher(returnString);
            if(matcher.find()){
                if(matcher.group(1).equals("本学期考试安排未发布！")){
                    mExamList.setVisibility(View.INVISIBLE);
                    mNoText.setText("暂无考试信息");
                }
            }
            else {
                //TODO: wait until exam schedule shows up
                mNoText.setVisibility(View.GONE);

            }
        }else if(o.getClass() == Integer.class){
            Integer code = (Integer)o;
            if(code == 302){
                this.startActivity(new Intent(getActivity(),EduLoginActivity.class));
                getActivity().finish();
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
            return Information.examCount;
        }

        @Override
        public Object getItem(int position) {
            return Information.exams.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                //convertView = mInflater.inflate(R.layout.exam_list_item,null);
                holder = new ViewHolder();

                convertView.setTag(holder);//绑定ViewHolder对象
            }
            else{
                holder = (ViewHolder)convertView.getTag();//取出ViewHolder对象
            }

            return convertView;
        }

    }
    class ViewHolder{
        TextView name;
        TextView classRoom;
        TextView time;
    }
    private boolean storeCourses(){
        SharedPreferences settings = getActivity().getSharedPreferences(Information.EXAM_PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("selectedCourseCount", String.valueOf(Information.examCount));
        for(int i = 0;i < Information.examCount;i++){

        }
        return editor.commit();
    }
}
