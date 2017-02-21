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
import static com.kongx.nkuassistant.Information.password;

public class EduLoginActivity extends AppCompatActivity implements Connector.Callback{
    private static final long mBackPressThreshold = 3500;
    static String m_username;
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
            if(Information.password != null){
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
        else Connector.getInformation(Connector.RequestType.CHECK_FOR_UPDATE,this,null);
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - mLastBackPress) > mBackPressThreshold) {
            Toast.makeText(getApplicationContext(), R.string.press_back_again_to_exit, Toast.LENGTH_SHORT).show();
            mLastBackPress = currentTime;
        } else finish();
    }

    @Override
    public void onConnectorComplete(Connector.RequestType requestType, final Object result) {
        SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        switch (requestType){
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
                                Toast.makeText(EduLoginActivity.this, "更新开始下载...", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).show();
            }
                break;
            case LOGIN:
                if(result.getClass() == Boolean.class && (Boolean)result) {                //Login Successfully
                    Connector.getInformation(Connector.RequestType.USER_INFO,this,null);
//                new Request.Builder().url(Information.WEB_URL + Strings.url_student_info + Information.getTimeStamp()).tag(RequestType.USER_INFO).build().send(null,this);
                    CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
                    for (HttpCookie httpCookie : cookieManager.getCookieStore().getCookies()) {
                        if (httpCookie.getName().equals("JSESSIONID")   ) {
                            editor.putString("JSESSIONID", httpCookie.getValue());
                            break;
                        }
                    }
                    editor.putBoolean(Strings.setting_remember_pwd, mRemPass.isChecked());
                    editor.putString(Strings.setting_studentID, m_username);
                    editor.putString(Strings.setting_password, Information.password);
                    editor.apply();
                }else {                             //Login Failed
                    String tmpString = (String) result;
                    if(tmpString.equals("密码错误"))    {
                        Toast.makeText(getApplicationContext(),Strings.str_wrong_password,Toast.LENGTH_SHORT).show();
                        Connect.initialize(new CookieManager());
                    }else if(tmpString.equals("failed"))    Toast.makeText(getApplicationContext(),Strings.str_login_failed,Toast.LENGTH_SHORT).show();
                }
                break;
            case USER_MAJOR_INFO:
                settings = getSharedPreferences(Information.PREFS_NAME, 0);
                editor = settings.edit();
                editor.putString(Strings.setting_student_name, Information.name);
                editor.putString(Strings.setting_student_faculty, Information.facultyName);
                editor.putString(Strings.setting_student_major, Information.majorName);
                editor.putBoolean(Strings.setting_student_isDoubleMajor,Information.isDoubleMajor);
                editor.apply();
                if(Information.isDoubleMajor)   Connector.getInformation(Connector.RequestType.USER_MINOR_INFO,this,null);
                else{
                    Information.ifLoggedIn = true;
                    startIndexActivity();
                }
                break;
            case USER_MINOR_INFO:
                settings = getSharedPreferences(Information.PREFS_NAME, 0);
                editor = settings.edit();
                editor.putString(Strings.setting_student_minor,Information.minorName);
                editor.apply();
                Information.ifLoggedIn = true;
                startIndexActivity();
                break;
        }

    }

    private void startIndexActivity(){
        Intent intent = new Intent(getApplicationContext(), IndexActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
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
            Information.password = settings.getString(Strings.setting_password, null);
        } else Information.password = password;
        String strToPost = String.format(Connector.login_string_template, m_username, Information.password);
        Connector.getInformation(Connector.RequestType.LOGIN,this,strToPost);
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

}

