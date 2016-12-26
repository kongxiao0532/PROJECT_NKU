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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;

import static com.kongx.nkuassistant.Information.PREFS_NAME;

public class IndexActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, Connectable{
    private Toast mPressBackToast;
    private long mLastBackPress;
    private static final long mBackPressThreshold = 3500;
    private HomeFragment homeFragment;
    private NavigationView navigationView;

    private static class RequestType{
        static final int CHECK_FOR_UPDATE = 0;
        static final int CHECK_FOR_NOTICE = 1;
    }
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
        fragmentTransaction.add(R.id.fragment_container,homeFragment,"HomeFragment");
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
        if(networkInfo == null) Toast.makeText(getApplicationContext(),R.string.connection_error, Toast.LENGTH_SHORT).show();
        else {
            new Connect(this, RequestType.CHECK_FOR_UPDATE, null).execute(Information.UPDATE_URL);
            new Connect(this, RequestType.CHECK_FOR_NOTICE, null).execute(Information.NOTICE_URL);
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
    public void onTaskComplete(Object o, int type) {
        if(o.getClass() == BufferedInputStream.class) {
            BufferedInputStream is = (BufferedInputStream) o;
            String retString;
            Pattern pattern;
            Matcher matcher;
            switch (type) {
                case RequestType.CHECK_FOR_UPDATE: {
                    retString = new Scanner(is).useDelimiter("\\A").next();
                    String versionNew = "";
                    String apkSize = "";
                    String updateTime = "";
                    String updateLog = "";
                    pattern = Pattern.compile("<version>(.+)(</version>)");
                    matcher = pattern.matcher(retString);
                    if(matcher.find())  versionNew = matcher.group(1);
                    pattern = Pattern.compile("<size>(.+)(</size>)");
                    matcher = pattern.matcher(retString);
                    if(matcher.find())  apkSize = matcher.group(1);
                    pattern = Pattern.compile("<updateTime>(.+)(</updateTime>)");
                    matcher = pattern.matcher(retString);
                    if(matcher.find())  updateTime = matcher.group(1);
                    pattern = Pattern.compile("<updateLog>(.+)(</updateLog>)");
                    matcher = pattern.matcher(retString);
                    if(matcher.find())  updateLog = matcher.group(1);
                    pattern = Pattern.compile("<downloadLink>(.+)(</downloadLink>)");
                    matcher = pattern.matcher(retString);
                    matcher.find();
                    final String downloadLink = matcher.group(1);
                    if (Information.version.equals(versionNew)){
                        break;
                    }
                    else {
                        new AlertDialog.Builder(this).setTitle(getString(R.string.update_available))
                                .setMessage("新版本："+versionNew+"\n更新包大小："+apkSize+"\n更新时间："+updateTime+"\n更新内容："+updateLog)
                                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri uri = Uri.parse(downloadLink);
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
                }
                case RequestType.CHECK_FOR_NOTICE: {
                    SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME, 0);
                    Information.newestNotice = settings.getString("newestNotice", null) == null ? -1 : Integer.parseInt(settings.getString("newestNotice", null));
                    retString = new Scanner(is).useDelimiter("\\A").next();
                    pattern = Pattern.compile("<id>([0-9])(</id>)");
                    matcher = pattern.matcher(retString);
                    matcher.find();
                    int tmpId = Integer.parseInt(matcher.group(1));
                    if (Information.newestNotice == tmpId) {
                        return;
                    } else {
                        Information.newestNotice = tmpId;
                        pattern = Pattern.compile("<headline>(.+)(</headline>)");
                        matcher = pattern.matcher(retString);
                        matcher.find();
                        String tmpHeadline = matcher.group(1);
                        pattern = Pattern.compile("<content>(.+)(</content>)");
                        matcher = pattern.matcher(retString);
                        matcher.find();
                        String tmpContent = matcher.group(1);
                        new AlertDialog.Builder(this).setTitle(tmpHeadline)
                                .setMessage(tmpContent)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).show();
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("newestNotice", String.valueOf(Information.newestNotice));
                        editor.apply();
                    }
                    break;
                }
                default:
                    break;
            }
        }else if(o.getClass() == SocketTimeoutException.class){
            Log.e("APP","SocketTimeoutException!");
        }
    }

    public void onClick(View view){
        if(view.getId() == R.id.home_schedule_details) {
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_schedule));
        }else if(view.getId() == R.id.home_exam_details){
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_exam));
        }else if(view.getId() == R.id.home_score_details){
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_score));
        }else if(view.getId() == R.id.home_select_details){
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_select));
        }else if(view.getId() == R.id.home_bus_details){
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_612bus));
        }
    }

    public void headerClicked(View view){
        startActivity(new Intent(getApplicationContext(),PersonalPage.class));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(getFragmentManager().getBackStackEntryCount() > 1){
            if(BrowserFragment.hasInstance()){
                return;
            }
            navigationView.setCheckedItem(R.id.nav_home);
            getFragmentManager().popBackStack();
        }
        else {
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

        if(Information.bugCheckFile != null){
            MenuItem menuItem = menu.findItem(R.id.action_bugreport);
            menuItem.setTitle(R.string.action_report_bug);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_TEXT,"PROJECT NKU 南开信息集中平台。下载地址：http://kongxiao0532.cn/projectnku/");
            share.setType("text/plain");
            startActivity(share);
            return true;
        }else if(id == R.id.action_bugreport){
            if(Information.bugCheckFile == null) {
                new AlertDialog.Builder(this).setTitle(getString(R.string.action_start_record))
                        .setMessage("启用日志记录后，程序将开始记录运行状态，如果程序崩溃或得到非预期结果，再次点击此按钮可发送日志内容给开发者")
                        .setPositiveButton("嗯=w=", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File bugCheckFile = new File(getExternalCacheDir(), new Date().getTime() + ".txt");
                                try {
                                    bugCheckFile.createNewFile();
                                } catch (IOException e) {
                                }
                                Connect.initializeBugCheck(bugCheckFile);
                                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                Information.bugCheckFile = bugCheckFile.getAbsolutePath();
                                editor.putString("lastBugCheckFile", Information.bugCheckFile);
                                editor.apply();
                                Connect.writeToBugCheck("Initialized with file " + bugCheckFile.getName());
                                item.setTitle(R.string.action_report_bug);
                            }
                        }).setNegativeButton("取消", null).show();
            }else {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.remove("lastBugCheckFile");
                editor.apply();
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_STREAM
                        , Uri.fromFile(new File(Information.bugCheckFile))
                );
                share.setType("*/*");
                startActivity(share);
            }
            return true;
        }
        else return super.onOptionsItemSelected(item);
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
            } else if (id == R.id.nav_exam) {
                fragmentTransaction.replace(R.id.fragment_container, new ExamFragment());
            } else if (id == R.id.nav_schedule) {
                fragmentTransaction.replace(R.id.fragment_container, new ScheduleFragment());
            } else if (id == R.id.nav_score) {
                fragmentTransaction.replace(R.id.fragment_container, new ScoreFragment());
            } else if (id == R.id.nav_select) {
                fragmentTransaction.replace(R.id.fragment_container, new SelectFragment());
            } else if (id == R.id.nav_about) {
                fragmentTransaction.replace(R.id.fragment_container, new AboutFragment());
            } else if (id == R.id.nav_612bus) {
                fragmentTransaction.replace(R.id.fragment_container, new ShuttleBusFragment());
            }else if (id == R.id.nav_feedback) {
                fragmentTransaction.replace(R.id.fragment_container, new FeedbackFragment());
            }else if (id == R.id.nav_icHome) {
                fragmentTransaction.replace(R.id.fragment_container, BrowserFragment.newInstance("http://ic.lib.nankai.edu.cn/ClientWeb/m/ic2/Default.aspx"));
            }else if (id == R.id.nav_tycg) {
                fragmentTransaction.replace(R.id.fragment_container, BrowserFragment.newInstance("http://tycg.nankai.edu.cn/"));
            }
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
