package com.kongx.nkuassistant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tk.sunrisefox.httprequest.Connect;
import tk.sunrisefox.httprequest.Request;
import tk.sunrisefox.httprequest.Response;

import static com.kongx.nkuassistant.Information.Strings;

public class EduLoginActivity extends AppCompatActivity implements Connect.Callback{
    private static final long mBackPressThreshold = 3500;
    static String m_username;
    static String m_encryptedPassword;
    private long mLastBackPress;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private Button mLoginButton;
    private View mProgressView;
    private Switch mRemPass;
    private boolean useRememberedPWD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edu_login);
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            //TODO: Show update logs between versions
            Information.version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) { Information.version = "unknown"; }

        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mRemPass = (Switch) findViewById(R.id.switch_RemPass);
        mRemPass.setChecked(true);
        mLoginButton = (Button) findViewById(R.id.button_login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                attemptLogin();
                return true;
            }
        });
        mProgressView = findViewById(R.id.login_progress);
        SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME, 0);
        mUsernameView.setText(settings.getString(Strings.setting_studentID, null));
        if (useRememberedPWD = settings.getBoolean(Strings.setting_remember_pwd, false)) {
            mRemPass.setChecked(true);
            mPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            if(settings.getString(Strings.setting_password,null) != null){
                mPasswordView.setText(Strings.str_pwd_not_changed);
            }
        }
        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mPasswordView.setOnFocusChangeListener(null);
                    mPasswordView.selectAll();
                    mPasswordView.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mPasswordView, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
        mPasswordView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { useRememberedPWD = false; }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void afterTextChanged(Editable editable) { }
        });
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo == null) Toast.makeText(getApplicationContext(),R.string.connection_error, Toast.LENGTH_SHORT).show();
        else new Request.Builder().url(Information.UPDATE_URL).tag(RequestType.CHECK_FOR_UPDATE).build().send(this);
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - mLastBackPress) > mBackPressThreshold) {
            Toast.makeText(getApplicationContext(), R.string.press_back_again_to_exit, Toast.LENGTH_SHORT).show();
            mLastBackPress = currentTime;
        } else finish();
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onNetworkComplete(Response response) {
        Pattern pattern;
        Matcher matcher;

        if(response.tag().equals(RequestType.CHECK_FOR_UPDATE)){
            String retString = response.body();
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
            if (!Information.version.equals(versionNew)) {
                new AlertDialog.Builder(this).setTitle(getString(R.string.update_available))
                        .setMessage("新版本："+versionNew+"\n更新包大小："+apkSize+"\n更新时间："+updateTime+"\n更新内容："+updateLog)
                        .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse(downloadLink);
                                Intent intent =new Intent(Intent.ACTION_VIEW, uri);startActivity(intent);
                                Toast.makeText(EduLoginActivity.this, "更新开始下载...", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).show();
            }
        } else if(response.tag().equals(RequestType.LOGIN)){
            if(response.code() == 302){
                new Request.Builder().url(Information.WEB_URL + Strings.url_student_ids + Information.getTimeStamp()).tag(RequestType.USER_IDS).build().send(null,this);
                new Request.Builder().url(Information.WEB_URL + Strings.url_student_info + Information.getTimeStamp()).tag(RequestType.USER_INFO).build().send(null,this);

                SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor settingEditor = settings.edit();
                CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
                for (HttpCookie httpCookie : cookieManager.getCookieStore().getCookies()) {
                    if (httpCookie.getName().equals("JSESSIONID")   ) {
                        settingEditor.putString("JSESSIONID", httpCookie.getValue());
                        break;
                    }
                }
                settingEditor.putBoolean(Strings.setting_remember_pwd, mRemPass.isChecked());
                settingEditor.putString(Strings.setting_studentID, m_username);
                settingEditor.putString(Strings.setting_password, m_encryptedPassword);
                settingEditor.apply();
            } else {
                //TODO: Login to VPN or clear cookies
            }
        } else if(response.tag().equals(RequestType.USER_IDS)) {
            String returnString = response.body();
            pattern = Pattern.compile("(bg.form.addInput\\(form,\"ids\",\")(.+)(\"\\);)");
            matcher = pattern.matcher(returnString);
            if (matcher.find()) Information.ids = matcher.group(2);
            SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Strings.setting_studentIDs,Information.ids);
            editor.apply();
        } else if(response.tag().equals(RequestType.USER_INFO)){
            String returnString = response.body();
            pattern = Pattern.compile("<td width=\"25%\">(.+)</td>");
            matcher = pattern.matcher(returnString);
            if (matcher.find()) Information.id = matcher.group(1);
            pattern = Pattern.compile("<td>(.*)(</td>)");
            matcher = pattern.matcher(returnString);
            if (matcher.find()) Information.name = matcher.group(1);
            matcher.find();
            matcher.find();
            matcher.find();
            matcher.find();
            matcher.find();
            matcher.find();
            matcher.find();
            if (matcher.find()) Information.facultyName = matcher.group(1);
            if (matcher.find()) Information.majorName = matcher.group(1);
            SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Strings.setting_student_name, Information.name);
            editor.putString(Strings.setting_student_faculty, Information.facultyName);
            editor.putString(Strings.setting_student_major, Information.majorName);
            editor.apply();
            Information.ifLoggedIn = true;
            Intent intent = new Intent(getApplicationContext(), IndexActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else if(response.tag().equals(RequestType.LOG_TO_VPN)){
            //TODO: Log to VPN
        }
    }

    @Override
    public void onNetworkError(Exception exception) {

    }

    private void attemptLogin() {
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        boolean cancel = true;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_studentID_required));
            focusView = mUsernameView;
        } else if (username.length() != 7) {
            mUsernameView.setError(getString(R.string.error_invalid_studentID));
            focusView = mUsernameView;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_password_required));
            focusView = mPasswordView;
        } else cancel = false;

        if (cancel) {
            focusView.requestFocus();
            return;
        }

        m_username = username;
        SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME, 0);
        if (useRememberedPWD) {
            m_encryptedPassword = settings.getString(Strings.setting_password, null);
        } else m_encryptedPassword = password;
        String strToPost = String.format(Strings.login_string_template, m_username, m_encryptedPassword);
        new Request.Builder().url(Information.WEB_URL + Strings.url_login).requestBody(strToPost).tag(RequestType.LOGIN).build().send(this);
        showProgress(true);
    }

    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(10).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private static class RequestType {
        static final String CHECK_FOR_UPDATE = "Check for update";
        static final String LOGIN = "Login";
        static final String USER_IDS = "User ID";
        static final String USER_INFO = "User Info";
        static final String LOG_TO_VPN = "Login to VPN";

    }
}

