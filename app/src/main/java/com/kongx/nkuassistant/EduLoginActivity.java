package com.kongx.nkuassistant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.evgenii.jsevaluator.JsEvaluator;
import com.evgenii.jsevaluator.interfaces.JsCallback;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;

/**
 * A login screen that offers login via email/password.
 */
public class EduLoginActivity extends AppCompatActivity implements Connectable {

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mValidateView;
    private View mProgressView;
    private ImageView mValidateCode;
    private CheckBox mRemPass;
    private CheckBox mAutoLogin;
    private Toast pressBackToast;
    private Toast connectionErrorToast;
    private long mLastBackPress;
    private static final long mBackPressThreshold = 3500;
    String encryptedPassword;

    public static class RequestType{
        static final int VALIDATE_CODE = 0;
        static final int LOGIN = 1;
        static final int COMMON_MESSAGE = 2;
    };

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    public void onLoginClicked(View view) {
        attemptLogin();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edu_login);
        // Set up the login form.
        //new Connect(this).execute("http://222.30.49.10/ValidateCode");
        changeCode(null);
        pressBackToast = Toast.makeText(getApplicationContext(), R.string.press_back_again_to_exit,
                Toast.LENGTH_SHORT);
        mValidateCode = (ImageView) findViewById(R.id.imageView_ValidateCode);
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mValidateView = (EditText) findViewById(R.id.ValidateCode);
        mRemPass = (CheckBox) findViewById(R.id.checkBox_RemPass);
        mAutoLogin = (CheckBox) findViewById(R.id.checkBox_AutoLog);
        mAutoLogin.setOnClickListener(new CheckBox.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(mAutoLogin.isChecked()){
                    mRemPass.setChecked(true);
                }
            }
        });
        mRemPass.setOnClickListener(new CheckBox.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(!mRemPass.isChecked()){
                    mAutoLogin.setChecked(false);
                }
            }
        });
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME,0);
        SharedPreferences.Editor settingEditor = settings.edit();
        settingEditor.putBoolean("ifAutoLogin",false);
        mEmailView.setText(settings.getString("StudentID",null));
        if(settings.getBoolean("ifRemPass",false)){
            mRemPass.setChecked(settings.getBoolean("ifRemPass",false));
            mPasswordView.setText(settings.getString("Password",null));
        }

    }
    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() > 1){
            getFragmentManager().popBackStack();
        }
        else {
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
    public void changeCode(View view){
        new Connect(this,RequestType.VALIDATE_CODE,null).execute("http://222.30.49.10/ValidateCode");
    }
    @Override
    public void onTaskComplete(Object o, int type) {
        BufferedInputStream is = (BufferedInputStream)o ;
        if(is == null) { Log.e("APP","Maybe network error."); return;}
        switch (type){
            case RequestType.VALIDATE_CODE:
                Bitmap pic = BitmapFactory.decodeStream(is);
                if(pic == null) {
                    Log.e("APP","Decode is finished but picture is not valid.");
                }else {
                    Bitmap resized = Bitmap.createBitmap(pic, 0, 0, pic.getWidth() / 2, pic.getHeight());
                    resized.setWidth(75);
                    mValidateCode.setImageBitmap(resized);
                }
                break;
            case RequestType.LOGIN:

                break;
            case RequestType.COMMON_MESSAGE:
                break;
            default:
                break;
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String validateCode = mValidateView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Input check
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_studentID_required));
            focusView = mEmailView;
            cancel = true;
        } else if (email.length() != 7) {
            mEmailView.setError(getString(R.string.error_invalid_studentID));
            focusView = mEmailView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_password_required));
            focusView = mPasswordView;
            cancel = true;
        } else if(TextUtils.isEmpty(validateCode)){
            mPasswordView.setError(getString(R.string.error_validate_required));
            focusView = mPasswordView;
            cancel = true;
        } else if(validateCode.length() != 4){
            mEmailView.setError(getString(R.string.error_invalid_validate));
            focusView = mEmailView;
            cancel = true;
                    }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME,0);
            SharedPreferences.Editor settingEditor = settings.edit();
            settingEditor.putBoolean("ifRemPass",mRemPass.isChecked());
            settingEditor.putBoolean("ifAutoLogin",mAutoLogin.isChecked());
            settingEditor.putString("StudentID",email);
            if(mRemPass.isChecked())    {settingEditor.putString("Password",password);}
            settingEditor.apply();
            Information.ifAutoLogin = mAutoLogin.isChecked();
            JsEvaluator jsEvaluator = new JsEvaluator(this);
            jsEvaluator.callFunction(Information.js, new JsCallback(){
                @Override
                public void onResult(String s) {
                    encryptedPassword = s;
                }
            },"encryption",password);
//            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
//            ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
//            scriptEngine.eval(new FileReader("d:\\security.js"));
//
//            scriptEngine.eval("encryption(\"" + password + "\")");
//            String encryptedPwd = (String)scriptEngine.get("result");

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
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
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
        Intent intent = new Intent(getApplicationContext(), IndexActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}

