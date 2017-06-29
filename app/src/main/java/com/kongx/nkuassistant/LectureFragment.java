package com.kongx.nkuassistant;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kongx.javaclasses.Lecture;

import java.util.ArrayList;
import java.util.Calendar;


public class LectureFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Connector.Callback{
    private SwipeRefreshLayout mRefresh;
    private ListView mLectureList;
    private Activity m_activity;
    private MyAdapter adapter;
    private int todayLectureID;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_lecture, container, false);
        mRefresh = (SwipeRefreshLayout) myView.findViewById(R.id.lecture_refresh);
        mRefresh.setOnRefreshListener(this);
        mLectureList = (ListView) myView.findViewById(R.id.lecture_list);
        todayLectureID = -1;
        adapter = new MyAdapter(getActivity());
        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();
        m_activity = getActivity();
        if(Information.lectures == null){
            onRefresh();
        }else {
            onConnectorComplete(Connector.RequestType.LECTURE,true);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        m_activity = null;
    }

    public void onRefresh() {
        mRefresh.setRefreshing(true);
        Information.lectures = new ArrayList<>();
        Connector.getInformation(Connector.RequestType.LECTURE,this,null);
    }

    @Override
    public void onConnectorComplete(Connector.RequestType requestType, Object result) {
        if(result.getClass() == Boolean.class){
            if(m_activity == null) return;
            mRefresh.setRefreshing(false);
            if((Boolean)result){
                calculateToday();
                mLectureList.setAdapter(adapter);
                if(todayLectureID > 0)  todayLectureID -= 2;
                mLectureList.setSelection(todayLectureID);
                adapter.notifyDataSetChanged();
            }
            else Toast.makeText(m_activity, R.string.connection_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateToday(){
        int tmpID = 0;
        for (Lecture tmpLecture : Information.lectures) {
            tmpID++;
            int year = 0, month = 0, day = 0;
            if (tmpLecture.getDatetime() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(tmpLecture.getDatetime());
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);
            }
            if (year == Information.year && month == Information.month) {
                if (day <= Information.day) {
                    todayLectureID = tmpID;
                    break;
                }
            }
        }
    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() { return Information.lectures.size();        }
        @Override
        public Object getItem(int position) {
            return Information.lectures.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            convertView = mInflater.inflate(R.layout.item_lecture_list,null);
            holder = new ViewHolder();
            holder.topic = convertView.findViewById(R.id.lecture_list_item_topic);
            holder.time = convertView.findViewById(R.id.lecture_list_item_time);
            holder.location = convertView.findViewById(R.id.lecture_list_item_location);
            holder.lecturer = convertView.findViewById(R.id.lecture_list_item_lecturer);
            holder.topic.setText(Information.lectures.get(position).getTopic());
            holder.time.setText(Information.lectures.get(position).getDateTimeString());
            holder.location.setText(Information.lectures.get(position).getLocation());
            holder.lecturer.setText(Information.lectures.get(position).getLecturer());
            return convertView;
        }
    }
    class ViewHolder{
        TextView topic;
        TextView time;
        TextView location;
        TextView lecturer;
    }
}


