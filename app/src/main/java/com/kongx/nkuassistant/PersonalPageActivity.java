package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import tk.sunrisefox.httprequest.*;

public class PersonalPageActivity extends AppCompatActivity implements View.OnClickListener, Connect.Callback {

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.button_logOut){
            new Request.Builder().url(Connector.WEB_URL + Connector.url_logout).build().send(this);
//            java.net.CookieManager cookieManager = new java.net.CookieManager();
//            Connect.initialize(cookieManager);
        } else finish();
    }

    private void clearInfomationAndExit(){
        Information.ifRemPass = false;
        SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        Toast.makeText(getApplicationContext(), Information.Strings.str_logout_suc , Toast.LENGTH_SHORT).show();
        editor.putInt(Information.Strings.setting_selected_course_count,-1);
        editor.putInt(Information.Strings.setting_studied_course_count,-1);
        editor.putInt(Information.Strings.setting_exam_count,-1);
        editor.putBoolean(Information.Strings.setting_remember_pwd,false);
        editor.putString(Information.Strings.setting_student_major_IDs,null);
        editor.putString(Information.Strings.setting_student_minor_IDs,null);
        editor.apply();
        File file = new File(getApplicationContext().getApplicationInfo().dataDir,"app_webview/Cookies");
        Log.e("APP",file.getAbsolutePath());
        file.delete();
        Intent intent = new Intent(getApplicationContext(), EduLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public void onNetworkComplete(Response response) {
        if(response.code() == 302){
            clearInfomationAndExit();
        }else {
            Toast.makeText(getApplicationContext(), Information.Strings.str_logout_failed , Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNetworkError(Exception exception) {
        Connect.initialize(new java.net.CookieManager());
        clearInfomationAndExit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Information.sessionUseVPN) setTheme(R.style.AppTheme_NoActionBar_TranslucentStatus_VPN);
        setContentView(R.layout.activity_personal_page);

        View view = findViewById(android.R.id.content);
        view.setOnClickListener(this);

        Button logoutBtn = (Button) findViewById(R.id.button_logOut);
        logoutBtn.setOnClickListener(this);

        TextView nameTextView = (TextView) findViewById(R.id.PP_Name);
        nameTextView.setText(Information.name);

        TextView facultyTextView = (TextView) findViewById(R.id.PP_Faculty);
        facultyTextView.setText(Information.facultyName);

        TextView majorTextView = (TextView) findViewById(R.id.PP_Major);
        majorTextView.setText(Information.majorName);

        TextView idTextView = (TextView) findViewById(R.id.PP_ID);
        idTextView.setText(Information.id);

        TextView gradeTextView = (TextView) findViewById(R.id.PP_Grade);
        gradeTextView.setText("20"+Information.id.substring(0,2)+"级本科生");

        if(Information.isDoubleMajor){
            TextView doubleTextView = (TextView) findViewById(R.id.PP_Double);
            gradeTextView.setText("第二专业："+Information.minorName);
        }
    }
}
