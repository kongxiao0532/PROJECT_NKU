package com.kongx.nkuassistant;

import android.app.Application;
import android.content.SharedPreferences;

//import com.xiaomi.market.sdk.XiaomiUpdateAgent;

import java.net.CookieManager;
import java.net.CookiePolicy;

import cn.jiguang.analytics.android.api.JAnalyticsInterface;
import cn.jpush.android.api.JPushInterface;
import tk.sunrisefox.httprequest.Connect;

/**
 * Created by DELL on 2017/3/8 0008.
 */

public class NKUApplication extends Application {



    @Override
    public void onCreate() {
        super.onCreate();

//        XiaomiUpdateAgent.update(this);



        //JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        JAnalyticsInterface.init(this);
        //JAnalyticsInterface.setDebugMode(true);


        //Initialize network
        System.setProperty("java.net.useSystemProxies", "true");
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        Connect.initialize(cookieManager);
        Connect.addDefaultHeaders("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36 PROJECT");
    }


}
