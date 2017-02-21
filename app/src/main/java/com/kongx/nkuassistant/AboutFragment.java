package com.kongx.nkuassistant;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

public class AboutFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        ViewPager mImageViewPager = (ViewPager) view.findViewById(R.id.pager);
        mImageViewPager.setAdapter(new ScreenSlidePagerAdapter(((AppCompatActivity)getActivity()).getSupportFragmentManager()));

        final TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(mImageViewPager, true);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ViewGroup vg = ((ViewGroup) tabLayout.getChildAt(0));
                vg.setAlpha(0.8f);
                vg.animate()
                        .alpha(0.4f)
                        .setStartDelay(600)
                        .setDuration(400)
                        .setInterpolator(new LinearOutSlowInInterpolator())
                        .start();

                ViewGroup vgTab = (ViewGroup) vg.getChildAt(tab.getPosition());
                vgTab.setScaleX(0.8f);
                vgTab.setScaleY(0.8f);
                vgTab.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .setDuration(450)
                        .start();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

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


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if(position==1) return new PageAboutFoxFragment();
            return new PageAboutKongxFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
