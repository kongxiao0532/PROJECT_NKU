package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PersonalPage extends AppCompatActivity implements View.OnClickListener {
    private TextView mNameView;
    private TextView mFacultyView;
    private TextView mMajorView;
    private TextView mStudentIDView;
    private TextView mGradeView;
    private Button mLogoutButton;

    @Override
    public void onClick(View view) {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_page);
        View view = findViewById(android.R.id.content);
        view.setOnClickListener(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLogoutButton = (Button) findViewById(R.id.button_logOut);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME,0);
//                SharedPreferences.Editor settingEditor = settings.edit();
//                settingEditor.putBoolean("ifRemPass",false);
//                settingEditor.apply();
                IndexActivity.thisIndexPtr.finish();
                Intent intent = new Intent(getApplicationContext(), EduLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        mNameView = (TextView) findViewById(R.id.PP_Name);
        mFacultyView = (TextView) findViewById(R.id.PP_Faculty);
        mMajorView = (TextView) findViewById(R.id.PP_Major);
        mStudentIDView = (TextView) findViewById(R.id.PP_ID);
        mGradeView = (TextView) findViewById(R.id.PP_Grade);
        mNameView.setText(Information.name);
        mFacultyView.setText(Information.facultyName);
        mMajorView.setText(Information.majorName);
        mStudentIDView.setText(Information.id);
        mGradeView.setText("20"+Information.id.substring(0,2)+"级本科生");
    }
}
