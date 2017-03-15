package com.kongx.nkuassistant;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.DrawableRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;


public class  ShuttleBusFragment extends Fragment {
    private ListView mToJinnanList;
    private ListView mToBalitaiList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shuttle_bus, container, false);

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setAdapter(new ScreenSlidePagerAdapter(((AppCompatActivity)getActivity()).getSupportFragmentManager()));

        final TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager, true);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ViewGroup vg = ((ViewGroup) tabLayout.getChildAt(0));
                vg.animate()
                        .setStartDelay(600)
                        .setDuration(400)
                        .setInterpolator(new LinearOutSlowInInterpolator())
                        .start();

                ViewGroup vgTab = (ViewGroup) vg.getChildAt(tab.getPosition());
                vgTab.setScaleX(0.8f);
                vgTab.setScaleY(0.8f);
                vgTab.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .setDuration(450)
                        .start();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        return view;
    }

    public static class ShuttleBusPage1 extends android.support.v4.app.Fragment{
        public ShuttleBusPage1(){}
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_shuttle_bus_page1, container, false);
            ListView list = (ListView)view.findViewById(R.id.list_tojinnan);
            ToJinnanAdapter adapter = new ToJinnanAdapter(getActivity());
            list.setAdapter(adapter);
            list.setSelection(Information.toJinnanID == -1 ? 0 : Information.toJinnanID);
            adapter.notifyDataSetChanged();
            return view;
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
                ViewHolder holder;
                convertView = mInflater.inflate(R.layout.bus_timetable_item,null);
                holder = new ViewHolder();
                holder.way = (TextView) convertView.findViewById(R.id.textView_way);
                holder.time = (TextView) convertView.findViewById(R.id.textView_time);
                if(position == Information.toJinnanID) {
                    holder.way.setTextColor(getActivity().getResources().getColorStateList(R.color.colorPrimary));
                    holder.time.setTextColor(getActivity().getResources().getColorStateList(R.color.colorPrimary));
                }
                holder.way.setText(Information.weekdays_tojinnan.get(position).get("way") == 1 ? "点对点" : "快线");
                holder.time.setText(Information.weekdays_tojinnan.get(position).get("hour") + ":" +
                        ((Information.weekdays_tojinnan.get(position).get("minute") == 0) ? "00" : Information.weekdays_tojinnan.get(position).get("minute")));
                return convertView;
            }
        }

        class ViewHolder {
            TextView way;
            TextView time;
        }
    }

    public static class ShuttleBusPage2 extends android.support.v4.app.Fragment{
        public ShuttleBusPage2(){}
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_shuttle_bus_page2, container, false);
            ListView list = (ListView)view.findViewById(R.id.list_tobalitai);
            ToBalitaiAdapter adapter = new ToBalitaiAdapter(getActivity());
            list.setAdapter(adapter);
            list.setSelection(Information.toBalitaiID == -1 ? 0 : Information.toBalitaiID);
            adapter.notifyDataSetChanged();
            return view;
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
                ViewHolder holder;
                convertView = mInflater.inflate(R.layout.bus_timetable_item,null);
                holder = new ViewHolder();
                holder.way = (TextView) convertView.findViewById(R.id.textView_way);
                holder.time = (TextView) convertView.findViewById(R.id.textView_time);
                if(position == Information.toBalitaiID) {
                    holder.way.setTextColor(getActivity().getResources().getColorStateList(R.color.colorPrimary));
                    holder.time.setTextColor(getActivity().getResources().getColorStateList(R.color.colorPrimary));
                }
                holder.way.setText(Information.weekdays_tobalitai.get(position).get("way") == 1 ? "点对点" : "快线");
                holder.time.setText(Information.weekdays_tobalitai.get(position).get("hour") + ":" +
                        ((Information.weekdays_tobalitai.get(position).get("minute") == 0) ? "00" : Information.weekdays_tobalitai.get(position).get("minute")));
                return convertView;
            }
        }
        class ViewHolder {
            TextView way;
            TextView time;
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if(position==1) return new ShuttleBusPage2();
            return new ShuttleBusPage1();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position==1) return getString(R.string.tab_tobalitai);
            return getString(R.string.tab_tojinnan);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
