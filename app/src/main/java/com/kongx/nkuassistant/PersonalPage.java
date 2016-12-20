package com.kongx.nkuassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PersonalPage extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.button_logOut){
            Information.selectedCourseCount = -1;
            Information.studiedCourseCount = -1;
            Information.examCount = -1;
            Information.ifLoggedIn = false;
            Information.ifRemPass = false;
            SharedPreferences settings = getSharedPreferences(Information.PREFS_NAME,0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Information.Strings.setting_remember_pwd,false);
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), EduLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finishAffinity();
        } else finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
}
