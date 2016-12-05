package com.kongx.nkuassistant;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class ShuttleBusFragment extends Fragment {
    private View myView = null;
    private TabHost mTab;
    private ListView mToJinnanList;
    private ListView mToBalitaiList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_shuttle_bus, container, false);
        mTab = (TabHost) myView.findViewById(R.id.shuttlebus_tabs);
        mTab.setup();
        TabHost.TabSpec ts = mTab.newTabSpec("tag1");
        ts.setContent(R.id.tab_tojinnan);
        ts.setIndicator(getString(R.string.tab_tojinnan));
        mTab.addTab(ts);
        ts = mTab.newTabSpec("tag2");
        ts.setContent(R.id.tab_tobalitai);
        ts.setIndicator(getString(R.string.tab_tobalitai));
        mTab.addTab(ts);

        mToJinnanList = (ListView) myView.findViewById(R.id.list_tojinnan);
        mToBalitaiList = (ListView) myView.findViewById(R.id.list_tobalitai);
        mToJinnanList.setAdapter(new ToJinnanAdapter(getActivity()));
        mToBalitaiList.setAdapter(new ToBalitaiAdapter(getActivity()));

        return myView;
    }
    private class ToJinnanAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public ToJinnanAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return Information.weekdays_tojinnan.size();
        }
        @Override
        public Object getItem(int position) {
            return Information.weekdays_tojinnan.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ToJinnanViewHolder holder;
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.bus_timetable_item,null);
                holder = new ToJinnanViewHolder();
                holder.way = (TextView) convertView.findViewById(R.id.textView_way);
                holder.time = (TextView) convertView.findViewById(R.id.textView_time);
                convertView.setTag(holder);//绑定ViewHolder对象
            } else{
                holder = (ToJinnanViewHolder)convertView.getTag();//取出ViewHolder对象
            }
            holder.way.setText(Information.weekdays_tojinnan.get(position).get("way") == 1 ? "点对点" : "快线");
            holder.time.setText(Information.weekdays_tojinnan.get(position).get("hour") + ":" +
                    ((Information.weekdays_tojinnan.get(position).get("minute") == 0) ? "00" : Information.weekdays_tojinnan.get(position).get("minute")));
            return convertView;
        }
    }
    class ToJinnanViewHolder{
        TextView way;
        TextView time;
    }
    private class ToBalitaiAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public ToBalitaiAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return Information.weekdays_tobalitai.size();
        }
        @Override
        public Object getItem(int position) {
            return Information.weekdays_tobalitai.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ToBalitaiViewHolder holder;
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.bus_timetable_item,null);
                holder = new ToBalitaiViewHolder();
                holder.way = (TextView) convertView.findViewById(R.id.textView_way);
                holder.time = (TextView) convertView.findViewById(R.id.textView_time);
                convertView.setTag(holder);//绑定ViewHolder对象
            } else{
                holder = (ToBalitaiViewHolder)convertView.getTag();//取出ViewHolder对象
            }
            holder.way.setText(Information.weekdays_tobalitai.get(position).get("way") == 1 ? "点对点" : "快线");
            holder.time.setText(Information.weekdays_tobalitai.get(position).get("hour") + ":" +
                    ((Information.weekdays_tobalitai.get(position).get("minute") == 0) ? "00" : Information.weekdays_tobalitai.get(position).get("minute")));
            return convertView;
        }
    }
    class ToBalitaiViewHolder{
        TextView way;
        TextView time;
    }
}
