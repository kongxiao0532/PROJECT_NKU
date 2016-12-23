package com.kongx.nkuassistant;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class ICFragment extends Fragment {
   private View myView = null;
    private WebView webView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_ic, container, false);
        webView = (WebView) myView.findViewById(R.id.ic_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://ic.lib.nankai.edu.cn/ClientWeb/m/ic2/Default.aspx");
        return myView;
    }
}
