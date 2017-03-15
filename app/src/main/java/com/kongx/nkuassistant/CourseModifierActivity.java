package com.kongx.nkuassistant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class CourseModifierActivity extends AppCompatActivity {
    private Button a_button,c_button;
    private int index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_course_modifier);
        a_button = (Button) findViewById(R.id.modifier_accpet_button);
        c_button = (Button) findViewById(R.id.modifier_dismiss_button);
        c_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}
