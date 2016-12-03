package com.kongx.nkuassistant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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

/**
 * A login screen that offers login via username/password.
 */
public class EduLoginActivity extends AppCompatActivity implements Connectable {

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mValidateView;
    private View mProgressView;
    private ImageView mValidateCode;
    private CheckBox mRemPass;
    private Toast pressBackToast;
    private Toast connectionErrorToast;
    private long mLastBackPress;
    private static final long mBackPressThreshold = 3500;
    private Pattern pattern;
    private Matcher matcher;
    private WebView webView;
    static String m_username;
    static String m_validateCode;
    private static class RequestType {
        static final int VALIDATE_CODE = 0;
        static final int LOGIN = 1;
        static final int USER_INFO = 2;
    }


    public void onLoginClicked(View view) {
        attemptLogin();
    }

    class JSInterface{
        JSInterface(){}
        @android.webkit.JavascriptInterface
        void updatePwd(String echo){
            String template = "operation=&usercode_text=%s&userpwd_text=%s&checkcode_text=%s&submittype=%%C8%%B7+%%C8%%CF";
            String strToPost = String.format(template, m_username, echo, m_validateCode);
            new Connect(EduLoginActivity.this, RequestType.LOGIN, strToPost).execute(Information.webUrl+"/stdloginAction.do");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edu_login);
        changeCode(null);
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JSInterface(),"Echopwd");
        webView.loadUrl("file:///android_asset/encryptpwd.html");
        mValidateCode = (ImageView) findViewById(R.id.imageView_ValidateCode);
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mValidateView = (EditText) findViewById(R.id.ValidateCode);
        mRemPass = (CheckBox) findViewById(R.id.checkBox_RemPass);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mProgressView = findViewById(R.id.login_progress);
        SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME, 0);
        SharedPreferences.Editor settingEditor = settings.edit();
        settingEditor.putBoolean("ifFirstStart", false);
        settingEditor.apply();
        mUsernameView.setText(settings.getString("StudentID", null));
        if (settings.getBoolean("ifRemPass", false)) {
            mRemPass.setChecked(settings.getBoolean("ifRemPass", false));
            mPasswordView.setText(settings.getString("Password", null));
        }
    }

    @Override
    public void onBackPressed() {
        pressBackToast = Toast.makeText(getApplicationContext(), R.string.press_back_again_to_exit,
                Toast.LENGTH_SHORT);
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
        } else {
            long currentTime = System.currentTimeMillis();
            if (Math.abs(currentTime - mLastBackPress) > mBackPressThreshold) {
                pressBackToast.show();
                mLastBackPress = currentTime;
            } else {
                pressBackToast.cancel();
                finish();
            }
        }
    }

    public void changeCode(View view) {
        new Connect(this, RequestType.VALIDATE_CODE, null).execute(Information.webUrl+"/ValidateCode");
    }

    @Override
    public void onTaskComplete(Object o, int type) {
        if(o == null){
            Log.e("APP", "What the fuck?");
        }else if(o.getClass() == BufferedInputStream.class) {
            BufferedInputStream is = (BufferedInputStream) o;
            switch (type) {
                case RequestType.VALIDATE_CODE:
                    Bitmap pic = BitmapFactory.decodeStream(is);
                    if (pic == null) {
                        Log.e("APP", "Decode is finished but picture is not valid.");
                    } else {
                        Bitmap resized = Bitmap.createBitmap(pic, 0, 0, pic.getWidth() / 2, pic.getHeight());
                        resized.setWidth(75);
                        mValidateCode.setImageBitmap(resized);
                    }
                    break;
                case RequestType.LOGIN: {
                    String returnString = new Scanner(is, "GB2312").useDelimiter("\\A").next();
                    pattern = Pattern.compile("<LI>(.+)\\n(?=</LI>)");
                    matcher = pattern.matcher(returnString);
                    if (matcher.find()) {
                        Toast.makeText(getApplicationContext(), "登录失败，" + matcher.group(1), Toast.LENGTH_SHORT).show();
                        mValidateView.setText("");
                        changeCode(null);
                        showProgress(false);
                    } else {
                        CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
                        for (HttpCookie httpCookie : cookieManager.getCookieStore().getCookies()) {
                            if (httpCookie.getName().equals("JSESSIONID")   ) {
                                SharedPreferences preferences = getSharedPreferences(Information.PREFS_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("JSESSIONID", httpCookie.getValue());
                                editor.apply();
                            }
                        }
                        new Connect(this, RequestType.USER_INFO, null).execute(Information.webUrl + "/studymanager/stdbaseinfo/queryAction.do");
                    }
                    break;
                }
                case RequestType.USER_INFO: {
                    String returnString = new Scanner(is, "GB2312").useDelimiter("\\A").next();
                    pattern = Pattern.compile("<td width=\"10%\" bgcolor=\"#ffffff\" class=\"NavText\">(.+)</td>");
                    matcher = pattern.matcher(returnString);
                    if (matcher.find()) {
                        Information.id = matcher.group(1);
                    }
                    if (matcher.find()) {
                        Information.name = matcher.group(1);

                    }
                    pattern = Pattern.compile("<td  bgcolor=\"#ffffff\" class=\"NavText\" colspan=\"3\">(.+)</td>");
                    matcher = pattern.matcher(returnString);
                    if (matcher.find()) {
                        Information.facultyName = matcher.group(1);
                    }
                    if (matcher.find()) {
                        Information.majorName = matcher.group(1);
                    }
                    SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("StudentName", Information.name);
                    editor.putString("FacultyName", Information.facultyName);
                    editor.putString("MajorName", Information.majorName);
                    editor.putString("StudentID", Information.id);
                    editor.apply();
                    Intent intent = new Intent(getApplicationContext(), IndexActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
                }
                default:
                    break;
            }
        }else if(o.getClass() == Integer.class){
            Integer code = (Integer)o;
            if(code == 302){
                //TODO:Login to 202.113.18.106
                Log.e("APP","Maybe not log in to NKU_WLAN");
                //this.startActivity(new Intent(null,EduLoginActivity.class));
                finish();
            }
        }else if(o.getClass() == SocketTimeoutException.class){
            Log.e("APP","SocketTimeoutException!");
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);
        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String validateCode = mValidateView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Input check
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_studentID_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (username.length() != 7) {
            mUsernameView.setError(getString(R.string.error_invalid_studentID));
            focusView = mUsernameView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_password_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (TextUtils.isEmpty(validateCode)) {
            mValidateView.setError(getString(R.string.error_validate_required));
            focusView = mValidateView;
            cancel = true;
        } else if (validateCode.length() != 4) {
            mValidateView.setError(getString(R.string.error_invalid_validate));
            focusView = mValidateView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME, 0);
            SharedPreferences.Editor settingEditor = settings.edit();
            settingEditor.putBoolean("ifRemPass", mRemPass.isChecked());
            settingEditor.putBoolean("ifFirstStart", true);
            settingEditor.putString("StudentID", username);
            if (mRemPass.isChecked()) {
                settingEditor.putString("Password", password);
            }
            settingEditor.apply();
            m_username = username;
            m_validateCode = validateCode;
            webView.loadUrl("javascript:encryption(\"" + password + "\")");

            showProgress(true);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

            //TODO:NOT FINISHED
            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            mProgressView.animate().setDuration(10).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                }
            });
        }
    }
}

