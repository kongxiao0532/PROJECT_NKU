package com.kongx.nkuassistant;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;


public class  ShuttleBusFragment extends Fragment {
    private ToggleButton m_toggle;
    private static boolean isWeekdays;
    private static ShuttleBusPage1.ToJinnanAdapter j_adapter;
    private static ShuttleBusPage2.ToBalitaiAdapter b_adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shuttle_bus, container, false);

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager_bus);
        viewPager.setAdapter(new ScreenSlidePagerAdapter(((AppCompatActivity)getActivity()).getSupportFragmentManager()));

        final TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager, true);
        m_toggle = (ToggleButton) view.findViewById(R.id.toggleButton);
        if(Information.dayOfWeek_int <= 5)  isWeekdays = true;
        else      isWeekdays = false;
        m_toggle.setChecked(isWeekdays);
        m_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isWeekdays = !isWeekdays;
                j_adapter.notifyDataSetChanged();
                b_adapter.notifyDataSetChanged();
            }
        });
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
            View view = inflater.inflate(R.layout.fragment_tab_page_list, container, false);
            ListView list = (ListView)view.findViewById(R.id.simple_list);
            j_adapter = new ToJinnanAdapter(getActivity());
            list.setAdapter(j_adapter);
            list.setSelection(Information.toJinnanID == -1 ? 0 : Information.toJinnanID);
            j_adapter.notifyDataSetChanged();
            return view;
        }

        private class ToJinnanAdapter extends BaseAdapter {
            private LayoutInflater mInflater;
            public ToJinnanAdapter(Context context) {
                this.mInflater = LayoutInflater.from(context);
            }
            @Override
            public int getCount() {
                return isWeekdays ? Information.weekdays_tojinnan.size() : Information.weekends_tojinnan.size();
            }
            @Override
            public Object getItem(int position) {
                return isWeekdays ? Information.weekdays_tojinnan.get(position) : Information.weekends_tojinnan.get(position);
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
                holder.image = (ImageView) convertView.findViewById(R.id.bus_item_image);
                holder.time = (TextView) convertView.findViewById(R.id.textView_time);
                if(position == Information.toJinnanID) {
                    holder.way.setTextColor(getActivity().getResources().getColorStateList(R.color.colorPrimary));
                    holder.image.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.nextbus));
                    holder.time.setTextColor(getActivity().getResources().getColorStateList(R.color.colorPrimary));
                }
                if(isWeekdays){
                    holder.way.setText(Information.weekdays_tojinnan.get(position).get("way") == 1 ? "点对点" : "快线");
                    holder.time.setText(Information.weekdays_tojinnan.get(position).get("hour") + ":" +
                            ((Information.weekdays_tojinnan.get(position).get("minute") == 0) ? "00" : Information.weekdays_tojinnan.get(position).get("minute")));
                }else {
                    holder.way.setText(Information.weekends_tojinnan.get(position).get("way") == 1 ? "点对点" : "快线");
                    holder.time.setText(Information.weekends_tojinnan.get(position).get("hour") + ":" +
                            ((Information.weekends_tojinnan.get(position).get("minute") == 0) ? "00" : Information.weekends_tojinnan.get(position).get("minute")));

                }
                return convertView;
            }
        }

        class ViewHolder {
            TextView way;
            ImageView image;
            TextView time;
        }
    }

    public static class ShuttleBusPage2 extends android.support.v4.app.Fragment{
        public ShuttleBusPage2(){}
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tab_page_list, container, false);
            ListView list = (ListView)view.findViewById(R.id.simple_list);
            b_adapter = new ToBalitaiAdapter(getActivity());
            list.setAdapter(b_adapter);
            list.setSelection(Information.toBalitaiID == -1 ? 0 : Information.toBalitaiID);
            b_adapter.notifyDataSetChanged();
            return view;
        }

        private class ToBalitaiAdapter extends BaseAdapter {
            private LayoutInflater mInflater;
            public ToBalitaiAdapter(Context context) {
                this.mInflater = LayoutInflater.from(context);
            }
            @Override
            public int getCount() {
                return isWeekdays ? Information.weekdays_tobalitai.size() : Information.weekends_tobalitai.size();
            }
            @Override
            public Object getItem(int position) {
                return isWeekdays ? Information.weekdays_tobalitai.get(position) : Information.weekends_tobalitai.get(position);
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
                holder.image = (ImageView) convertView.findViewById(R.id.bus_item_image);
                holder.time = (TextView) convertView.findViewById(R.id.textView_time);
                if(position == Information.toBalitaiID) {
                    holder.way.setTextColor(getActivity().getResources().getColorStateList(R.color.colorPrimary));
                    holder.image.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.nextbus));
                    holder.time.setTextColor(getActivity().getResources().getColorStateList(R.color.colorPrimary));
                }
                if(isWeekdays){
                    holder.way.setText(Information.weekdays_tobalitai.get(position).get("way") == 1 ? "点对点" : "快线");
                    holder.time.setText(Information.weekdays_tobalitai.get(position).get("hour") + ":" +
                            ((Information.weekdays_tobalitai.get(position).get("minute") == 0) ? "00" : Information.weekdays_tobalitai.get(position).get("minute")));
                }else {
                    holder.way.setText(Information.weekends_tobalitai.get(position).get("way") == 1 ? "点对点" : "快线");
                    holder.time.setText(Information.weekends_tobalitai.get(position).get("hour") + ":" +
                            ((Information.weekends_tobalitai.get(position).get("minute") == 0) ? "00" : Information.weekends_tobalitai.get(position).get("minute")));
                }
                return convertView;
            }
        }
        class ViewHolder {
            TextView way;
            ImageView image;
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
