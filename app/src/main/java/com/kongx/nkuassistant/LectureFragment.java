package com.kongx.nkuassistant;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jiguang.analytics.android.api.JAnalyticsInterface;
import tk.sunrisefox.httprequest.Connect;
import tk.sunrisefox.httprequest.Request;
import tk.sunrisefox.httprequest.Response;


public class LectureFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Connector.Callback{
    private SwipeRefreshLayout mRefresh;
    private ListView mLectureList;
    private Activity m_activity;
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

        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();
        m_activity = getActivity();
        JAnalyticsInterface.onPageStart(m_activity, this.getClass().getCanonicalName());
        if(Information.lectures == null){
            onRefresh();
        }else {
            onConnectorComplete(Connector.RequestType.LECTURE,true);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        JAnalyticsInterface.onPageEnd(getActivity(),this.getClass().getCanonicalName());
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
            if((Boolean)result)
                mLectureList.setAdapter(new MyAdapter(m_activity));
            else Toast.makeText(m_activity, R.string.connection_error, Toast.LENGTH_SHORT).show();
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
            convertView = mInflater.inflate(R.layout.lecture_list_item,null);
            holder = new ViewHolder();
            holder.topic = (TextView) convertView.findViewById(R.id.lecture_list_item_topic);
            holder.time = (TextView) convertView.findViewById(R.id.lecture_list_item_time);
            holder.location = (TextView) convertView.findViewById(R.id.lecture_list_item_location);
            holder.lecturer = (TextView) convertView.findViewById(R.id.lecture_list_item_lecturer);
            holder.topic.setText(Information.lectures.get(position).topic);
            holder.time.setText(Information.lectures.get(position).time);
            holder.location.setText(Information.lectures.get(position).location);
            holder.lecturer.setText(Information.lectures.get(position).lecturer);
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


