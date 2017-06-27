package com.kongx.nkuassistant;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FeedbackActivity extends AppCompatActivity implements Connector.Callback{
    ProgressDialog admitDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_api);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final EditText mTopic = (EditText) findViewById(R.id.feedback_topic);
        final EditText mContent = (EditText) findViewById(R.id.feedback_content);
        final EditText mContact = (EditText) findViewById(R.id.feedback_contact);
        Button mAdmit = (Button) findViewById(R.id.admit_button);
        mAdmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mContact.getText().toString().length() >= 100){
                    Toast.makeText(FeedbackActivity.this,"反馈意见需少于100字",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!mContact.getText().toString().contains("@")){
                    Toast.makeText(FeedbackActivity.this,"请输入正确的邮箱地址",Toast.LENGTH_SHORT).show();
                    return;
                }
                String strToPost = String.format(Connector.feedback_post_template,Information.version,Information.id,mTopic.getText(),mContent.getText(),mContact.getText());
                Connector.getInformation(Connector.RequestType.FEEDBACK,FeedbackActivity.this,strToPost);
                admitDialog = new ProgressDialog(FeedbackActivity.this);
                admitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                admitDialog.setMessage("正在提交...");
                admitDialog.setIndeterminate(true);
                admitDialog.show();
            }
        });
    }

    @Override
    public void onConnectorComplete(Connector.RequestType requestType, Object result) {
        String returnString = (String) result;
        admitDialog.dismiss();
        if(returnString.contains("Success")){
            new AlertDialog.Builder(this).setTitle("反馈成功").setMessage("我们将尽快解决您的问题")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        }else{
            admitDialog.dismiss();
            new AlertDialog.Builder(this).setTitle("提交失败").setMessage(returnString)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        }
    }
}
