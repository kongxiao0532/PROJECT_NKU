package com.kongx.nkuassistant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;


public class CurriculumFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Connector.Callback {
    //    private int numberOfPages;
    private SwipeRefreshLayout mRefresh;
    private ListView mlistView;
    private Activity m_activity;
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
        mNoCurrirulumView = (TextView) myView.findViewById(R.id.textView_noCurriculum);
        mlistView = (ListView) myView.findViewById(R.id.list_curriculum);
        mRefresh = (SwipeRefreshLayout) myView.findViewById(R.id.curriculum_refresh);
        mRefresh.setOnRefreshListener(CurriculumFragment.this);
        mNoCurrirulumView.setVisibility(View.GONE);
        m_activity = getActivity();
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(),CourseModifierActivity.class);
                intent.putExtra("index",i);
                startActivity(intent);
            }
        });
        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();
        m_activity = getActivity();
        if (Information.selectedCourseCount == -1) {
            onRefresh();
        } else if (Information.selectedCourseCount == 0) {
            mNoCurrirulumView.setVisibility(View.VISIBLE);
            mlistView.setVisibility(View.GONE);
        } else {
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
        new AlertDialog.Builder(m_activity).setTitle("请注意")
                .setMessage("刷新会导致课程信息与教务系统同步，您修改的课程表信息将不再保存。是否要继续刷新？")
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefresh.setRefreshing(true);
                        Connector.getInformation(Connector.RequestType.CURRICULUM,CurriculumFragment.this,null);
                    }
                }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRefresh.setRefreshing(false);
            }
        })
                .show();
    }
    public void onConnectorComplete(Connector.RequestType requestType, Object result) {
        if (m_activity == null) return;
        switch (requestType){
            case CURRICULUM:
                if(result.getClass() == Boolean.class && (Boolean)result){
                    Information.selectedCourses = Connector.tmpSelectedCourses;
                    Information.selectedCourseCount = Connector.tmpSelectedCourses.size();
                    Calendar calendar = Calendar.getInstance();
                    int minute = calendar.get(Calendar.MINUTE);
                    String time_now = String.format(Locale.US, "%2d:%2d".replace(' ','0'), calendar.get(Calendar.HOUR_OF_DAY), minute);
                    Information.curriculum_lastUpdate = Information.date + " " + time_now;
                    storeCourses();
                    mRefresh.setRefreshing(false);
                    mlistView.setAdapter(new MyAdapter(m_activity));
                }else if(result.getClass() == Boolean.class && !(Boolean)result) {
                    Toast.makeText(getActivity(),"动作太快啦，请重试",Toast.LENGTH_SHORT).show();
                    mRefresh.setRefreshing(false);
                }
                break;
            case LOGIN:
                if(result.getClass() == Boolean.class && (Boolean)result) {                //Login Successfully
                    Toast.makeText(getActivity(), "已重新登录", Toast.LENGTH_SHORT).show();
                    onRefresh();
                }else {
                    Toast.makeText(getActivity(), "重新登录失败", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(m_activity,EduLoginActivity.class));
                    m_activity.finish();
                }
                break;
            default:
                break;
        }
    }

    boolean storeCourses() {
        SharedPreferences settings = m_activity.getSharedPreferences(Information.COURSE_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("selectedCourseCount", Information.selectedCourseCount);
        for (int i = 0; i < Information.selectedCourseCount; i++) {
            editor.putString("index" + i, Information.selectedCourses.get(i).index);
            editor.putString("name" + i, Information.selectedCourses.get(i).name);
            editor.putInt("dayOfWeek" + i, Information.selectedCourses.get(i).dayOfWeek);
            editor.putInt("startTime" + i, Information.selectedCourses.get(i).startTime);
            editor.putInt("endTime" + i, Information.selectedCourses.get(i).endTime);
            editor.putString("classRoom" + i, Information.selectedCourses.get(i).classRoom);
//            editor.putString("classType" + i, Information.selectedCourses.get(i).classType);
            editor.putString("teacherName" + i, Information.selectedCourses.get(i).teacherName);
            editor.putInt("startWeek" + i, Information.selectedCourses.get(i).startWeek);
            editor.putInt("endWeek" + i, Information.selectedCourses.get(i).endWeek);
            editor.putInt("color" + i, Information.selectedCourses.get(i).color);
        }
        for(int i = 0;i < 14;i++){
            for(int j = 0;j < 7;j++){
                editor.putBoolean("isBusy"+i+j,Information.scheduleTimeIsBusy[i][j]);
            }
        }
        editor.putString("curriculum_lastUpdate", Information.curriculum_lastUpdate);
        return editor.commit();
    }

private class MyAdapter extends BaseAdapter {
    private LayoutInflater mInflater;

    MyAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return Information.selectedCourseCount + 1;
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
        if (position == Information.selectedCourseCount) {
            convertView = mInflater.inflate(R.layout.listview_lastupdate, null);
            TextView last_update_view = (TextView) convertView.findViewById(R.id.last_update);
            last_update_view.setText("最后更新：" + ((Information.curriculum_lastUpdate == null) ? "从未更新" : Information.curriculum_lastUpdate));
        } else {
            ViewHolder holder;
//                if(convertView == null){
            convertView = mInflater.inflate(R.layout.selected_list_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.selected_list_name);
            holder.day = (TextView) convertView.findViewById(R.id.selected_list_day);
            holder.index = (TextView) convertView.findViewById(R.id.selected_list_index);
            holder.time = (TextView) convertView.findViewById(R.id.selected_list_time);
            holder.name.setText(Information.selectedCourses.get(position).name + "（" + Information.selectedCourses.get(position).teacherName + "）");
            holder.day.setText(Information.dayOfWeek[Information.selectedCourses.get(position).dayOfWeek]);
            holder.index.setText(Information.selectedCourses.get(position).index);
            holder.time.setText(Information.startTime[Information.selectedCourses.get(position).startTime] + "-" +
                    Information.endTime[Information.selectedCourses.get(position).endTime]);
        }
        return convertView;
    }

}

class ViewHolder {
    TextView name;
    TextView day;
    TextView index;
    TextView time;
}
}
