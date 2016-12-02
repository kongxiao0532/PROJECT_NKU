package com.kongx.nkuassistant;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.IDNA;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectFragment extends Fragment implements Connectable {
    private View myView = null;
    private boolean ifOpen = false;
    private EditText mIndex_1;
    private EditText mIndex_2;
    private EditText mIndex_3;
    private EditText mIndex_4;
    private CheckBox mReRead;
    private CheckBox mMinor;
    private Button mSelect;
    private Button mDrop;
    private TextView mNotOpenWarning;
    public static class RequestType{
        static final int CHECK_IF_OPEN = 0;
        static final int POST = 1;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_select, container, false);
        mIndex_1 = (EditText) myView.findViewById(R.id.editText_index_1);
        mIndex_2 = (EditText) myView.findViewById(R.id.editText_index_2);
        mIndex_3 = (EditText) myView.findViewById(R.id.editText_index_3);
        mIndex_4 = (EditText) myView.findViewById(R.id.editText_index_4);
        mReRead = (CheckBox) myView.findViewById(R.id.checkBox_ReRead);
        mMinor = (CheckBox) myView.findViewById(R.id.checkBox_Minor);
        mSelect = (Button) myView.findViewById(R.id.button_select);
        mDrop = (Button) myView.findViewById(R.id.button_Drop);
        mNotOpenWarning = (TextView) myView.findViewById(R.id.textView_notOpenWarning);
        mNotOpenWarning.setVisibility(View.GONE);
        new Connect(SelectFragment.this, RequestType.CHECK_IF_OPEN,null).execute(Information.webUrl+"/xsxk/selectMianInitAction.do");
        mSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIndex_1.getText().toString().isEmpty()){
                    mIndex_1.setError(getString(R.string.error_number_required));
                    return;
                }
                if(!ifOpen){
                    Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                String template = "operation=xuanke&index=&xkxh1=%s&xkxh2=%s&xkxh3=%s&xkxh4=%s&xkxh5=%s&xkxh6=%s&courseindex=";
                String strToPost = String.format
                        (template,
                                mIndex_1.getText().toString(),
                                mIndex_2.getText().toString().isEmpty()?"":mIndex_2.getText().toString(),
                                mIndex_3.getText().toString().isEmpty()?"":mIndex_3.getText().toString(),
                                mIndex_4.getText().toString().isEmpty()?"":mIndex_4.getText().toString(),
                                mReRead.isChecked()?"selected":"",
                                mMinor.isChecked()?"selected":"");
                new Connect(SelectFragment.this,RequestType.POST,strToPost).execute(Information.webUrl+"/xsxk/swichAction.do");
            }
        });
        mSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIndex_1.getText().toString().isEmpty()){
                    mIndex_1.setError(getString(R.string.error_number_required));
                    mIndex_1.requestFocus();
                    return;
                }
                if(!ifOpen){
                    Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                String template = "operation=tuike&index=&xkxh1=%s&xkxh2=%s&xkxh3=%s&xkxh4=%s&xkxh5=%s&xkxh6=%s&courseindex=";
                String strToPost = String.format
                        (template,
                                mIndex_1.getText().toString(),
                                mIndex_2.getText().toString().isEmpty()?"":mIndex_2.getText().toString(),
                                mIndex_3.getText().toString().isEmpty()?"":mIndex_3.getText().toString(),
                                mIndex_4.getText().toString().isEmpty()?"":mIndex_4.getText().toString(),
                                mReRead.isChecked()?"selected":"",
                                mMinor.isChecked()?"selected":"");
                new Connect(SelectFragment.this,RequestType.POST,strToPost).execute(Information.webUrl+"/xsxk/swichAction.do");
            }
        });
        return myView;
    }

    @Override
    public void onTaskComplete(Object o, int type) {
        if(o == null){
            Log.e("APP", "What the fuck?");
        }else if(o.getClass() == BufferedInputStream.class) {
            BufferedInputStream is = (BufferedInputStream)o ;
            String returnString = new Scanner(is,"GB2312").useDelimiter("\\A").next();
            switch (type){
                case RequestType.CHECK_IF_OPEN:{
                    Pattern pattern = Pattern.compile("<strong>(.+)(</strong>)");
                    Matcher matcher = pattern.matcher(returnString);
                    if(matcher.find()){
                        if(matcher.group(1).equals("选课系统关闭")){
                            ifOpen = false;
                            Toast.makeText(getActivity(), "选课系统关闭", Toast.LENGTH_SHORT).show();
                            mNotOpenWarning.setVisibility(View.VISIBLE);
                        }else {
                            mNotOpenWarning.setVisibility(View.GONE);
                            ifOpen = true;
                        }
                    }
                    else mNotOpenWarning.setVisibility(View.GONE);
                    break;
                }
                case RequestType.POST:{
                    //TODO: deal with the return data
                    break;
                }
                default:
                    break;
            }
        }else if(o.getClass() == Integer.class){
            Integer code = (Integer)o;
            if(code == 302){
                this.startActivity(new Intent(getActivity(),EduLoginActivity.class));
                getActivity().finish();
            }
        }else if(o.getClass() == SocketTimeoutException.class){
            Log.e("APP","SocketTimeoutException!");
        }


    }
}



