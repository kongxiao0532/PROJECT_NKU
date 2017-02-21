package com.kongx.nkuassistant;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;
import tk.sunrisefox.httprequest.Connect;
import tk.sunrisefox.httprequest.Request;
import tk.sunrisefox.httprequest.Response;

public class IndexActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, Connector.Callback {
    private static final long mBackPressThreshold = 3500;
    private Toast mPressBackToast;
    private long mLastBackPress;
    private HomeFragment homeFragment;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        homeFragment = new HomeFragment();
        fragmentTransaction.add(R.id.fragment_container, homeFragment, "HomeFragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        mPressBackToast = Toast.makeText(getApplicationContext(), R.string.press_back_again_to_exit, Toast.LENGTH_SHORT);

        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            Information.version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null)
            Toast.makeText(getApplicationContext(), R.string.connection_error, Toast.LENGTH_SHORT).show();
        else {
            Connector.getInformation(Connector.RequestType.CHECK_FOR_UPDATE,this,null);
            Connector.getInformation(Connector.RequestType.CHECK_FOR_NOTICE,this,null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }

    @Override
    public void onConnectorComplete(Connector.RequestType requestType, Object result) {
        switch (requestType){
            case CHECK_FOR_NOTICE:{
                final String[] resultString = (String[]) result;
                if (resultString[3].equals("all") || (resultString[3].equals("specific") && resultString[4].equals(Information.version))) {
                    new AlertDialog.Builder(this).setTitle(resultString[1])
                            .setMessage(resultString[2])
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
                SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("newestNotice", String.valueOf(Information.newestNotice));
                editor.apply();
                break;
            }
            case CHECK_FOR_UPDATE:
                final String[] resultString = (String[]) result;
                if (!Information.version.equals(resultString[0])) {
                    new AlertDialog.Builder(this).setTitle(getString(R.string.update_available))
                            .setMessage(resultString[2])
                            .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Uri uri = Uri.parse(resultString[1]);
                                    Intent intent =new Intent(Intent.ACTION_VIEW, uri);startActivity(intent);
                                    Toast.makeText(IndexActivity.this, "更新开始下载...", Toast.LENGTH_SHORT).show();
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).show();
                }
                break;
            default:
                break;
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.home_schedule_details) {
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_schedule));
//        }else if(view.getId() == R.id.home_exam_details){
//            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_exam));
        } else if (view.getId() == R.id.home_score_details) {
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_score));
//        }else if(view.getId() == R.id.home_select_details){
//            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_select));
        } else if (view.getId() == R.id.home_bus_details) {
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_612bus));
        }
    }

    public void headerClicked(View view) {
        startActivity(new Intent(getApplicationContext(), PersonalPage.class));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getFragmentManager().getBackStackEntryCount() > 1) {
            if (BrowserFragment.hasInstance()) {
                return;
            }
            navigationView.setCheckedItem(R.id.nav_home);
            getFragmentManager().popBackStack();
        } else {
            long currentTime = System.currentTimeMillis();
            if (Math.abs(currentTime - mLastBackPress) > mBackPressThreshold) {
                mPressBackToast.show();
                mLastBackPress = currentTime;
            } else {
                mPressBackToast.cancel();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.index, menu);

        TextView nameTextView = (TextView) findViewById(R.id.drawer_Name);
        nameTextView.setText(Information.name);

        TextView facultyTextView = (TextView) findViewById(R.id.drawer_Faculty);
        facultyTextView.setText(Information.facultyName);

        TextView idTextView = (TextView) findViewById(R.id.drawer_ID);
        idTextView.setText(Information.id);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_TEXT, "PROJECT NKU 南开信息集中平台。下载地址：http://kongxiao0532.cn/projectnku/");
            share.setType("text/plain");
            startActivity(share);
            return true;
        } else if (id == R.id.action_bugreport) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM
                    , Uri.fromFile(new File(getSharedPreferences(Information.PREFS_NAME, 0).getString("lastBugCheckFile", "")))
            );
            share.setType("*/*");
            startActivity(share);
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            BrowserFragment.clearInstance();
            getFragmentManager().popBackStack();
        }
        navigationView.setCheckedItem(id);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.hide(homeFragment);
        if (id == R.id.nav_home) {
            fragmentTransaction.show(homeFragment);
        } else {
            if (id == R.id.nav_curriculum) {
                fragmentTransaction.replace(R.id.fragment_container, new CurriculumFragment());
//            } else if (id == R.id.nav_exam) {
//                fragmentTransaction.replace(R.id.fragment_container, new ExamFragment());
            } else if (id == R.id.nav_schedule) {
                fragmentTransaction.replace(R.id.fragment_container, new ScheduleFragment());
            } else if (id == R.id.nav_score) {
                fragmentTransaction.replace(R.id.fragment_container, new ScoreFragment());
//            } else if (id == R.id.nav_select) {
//                fragmentTransaction.replace(R.id.fragment_container, new SelectFragment());
            } else if (id == R.id.nav_about) {
                fragmentTransaction.replace(R.id.fragment_container, new AboutFragment());
            } else if (id == R.id.nav_612bus) {
                fragmentTransaction.replace(R.id.fragment_container, new ShuttleBusFragment());
            } else if (id == R.id.nav_feedback) {
                fragmentTransaction.replace(R.id.fragment_container, new FeedbackFragment());
            } else if (id == R.id.nav_icHome) {
                fragmentTransaction.replace(R.id.fragment_container, BrowserFragment.newInstance("http://ic.lib.nankai.edu.cn/ClientWeb/m/ic2/Default.aspx"));
            } else if (id == R.id.nav_tycg) {
                fragmentTransaction.replace(R.id.fragment_container, BrowserFragment.newInstance("http://tycg.nankai.edu.cn/"));
            }
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static class RequestType {
        static final String CHECK_FOR_UPDATE = "Check for update";
        static final String CHECK_FOR_NOTICE = "Check for notice";
    }
}
