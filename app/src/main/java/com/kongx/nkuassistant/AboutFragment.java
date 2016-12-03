package com.kongx.nkuassistant;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = (View) inflater.inflate(R.layout.fragment_about, container, false);
        TabHost tabhost = (TabHost) view.findViewById(android.R.id.tabhost);
        tabhost.setup();
        TabHost.TabSpec ts = tabhost.newTabSpec("tag1");
        ts.setContent(R.id.tab1);
        ts.setIndicator("First Tab");
        tabhost.addTab(ts);

        ts = tabhost.newTabSpec("tag2");
        ts.setContent(R.id.tab2);
        ts.setIndicator("Second Tab");
        tabhost.addTab(ts);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
