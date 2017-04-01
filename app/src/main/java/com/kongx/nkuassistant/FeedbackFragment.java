package com.kongx.nkuassistant;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import tk.sunrisefox.httprequest.Connect;
import tk.sunrisefox.httprequest.Request;


public class FeedbackFragment extends Fragment implements View.OnClickListener, Connector.Callback{
    private String version;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = (View) inflater.inflate(R.layout.fragment_feedback, container, false);
        Button mCheckForUpdate = (Button) myView.findViewById(R.id.button_check_for_update);
        Button mFeedback = (Button) myView.findViewById(R.id.button_feedback);
        TextView mVersion = (TextView) myView.findViewById(R.id.version);
        mCheckForUpdate.setOnClickListener(FeedbackFragment.this);
        mFeedback.setOnClickListener(FeedbackFragment.this);
        mVersion.setText(Information.version);
        return myView;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.button_check_for_update){
            Connector.getInformation(Connector.RequestType.CHECK_FOR_UPDATE,this,null);

        }else if(view.getId() == R.id.button_feedback){
            Intent feedback = new Intent(getActivity(),FeedbackApiActivity.class);
            startActivity(feedback);
//            Intent seedEmail=new Intent(Intent.ACTION_SENDTO);
//            seedEmail.setData(Uri.parse(Information.FEEDBACK_EMAIL));
//            seedEmail.putExtra(Intent.EXTRA_SUBJECT, Information.FEEDBACK_SUBJECT);
//            startActivity(seedEmail);
        }
    }

    @Override
    public void onConnectorComplete(Connector.RequestType requestType, Object result) {
        switch (requestType){
            case DOWNLOAD_UPDATE:
                File apkFile = (File) result;
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), "com.kongx.nkuassistant.fileprovider", apkFile);
                    install.setDataAndType(contentUri, "application/vnd.android.package-archive");
                } else {
                    install.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                }

                startActivity(install);
                break;
            case CHECK_FOR_UPDATE:
                final String[] resultString = (String[]) result;
                if (!Information.version.equals(resultString[0])) {
                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.update_available))
                            .setMessage(resultString[2])
                            .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    progressDialog.setTitle("正在下载更新...");
                                    progressDialog.setIndeterminate(false);
                                    progressDialog.setCancelable(true);
                                    new Request.Builder().progress(new Connect.Progress() {
                                        @Override
                                        public void updateProgress(Long current, Long total) {
                                            progressDialog.setMax(total.intValue()/1000);
                                            progressDialog.setProgress(current.intValue()/1000);
                                            if(current == total) progressDialog.dismiss();
                                        }
                                    }).url(resultString[1]).saveAsFile()
                                            .get(new Connector.UpdateDownloadConnector(FeedbackFragment.this));
                                    progressDialog.show();
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).show();
                }
                break;
        }
    }
}
