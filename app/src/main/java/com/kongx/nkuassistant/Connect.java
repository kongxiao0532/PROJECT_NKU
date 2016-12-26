package com.kongx.nkuassistant;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * This is the interface to handle all the classes's network requests.
 */
class Connect extends AsyncTask<String, Integer, Object> {
    private Connectable parent;
    private static final String DEBUG_TAG = "APP";
    private int type;
    private String postMessage;
    private static int timeoutTime = 3000;
    public static boolean debug_output = false;
    public Connect(Connectable parent, int type, @Nullable String post) {
        this.parent = parent;
        this.type = type;
        this.postMessage = post;
    }
    public static FileWriter bugCheckFileWriter = null;
    public static void initialize(CookieManager cookieManager){
        CookieHandler.setDefault(cookieManager);
    }
    public static void initializeBugCheck(File file){
        try { bugCheckFileWriter = new FileWriter(file); } catch (IOException e) { }
        debug_output = true;
    }
    public static void writeToBugCheck(String str){
        if(debug_output)
            try { bugCheckFileWriter.write(str+"\n"); bugCheckFileWriter.flush(); } catch (IOException e) { }
    }
    public static void serTimeout(int time){
        timeoutTime = time;
    }
    @Override
    protected Object doInBackground(String... urls) {
        Connect.writeToBugCheck(parent.getClass().getName() + " tries connecting type "+ type + " to " + urls[0]);
        Connect.writeToBugCheck("Posting " + postMessage);
        InputStream is = null;
        try {
            ProxySelector defaultProxySelector = ProxySelector.getDefault();
            Proxy proxy = null;
            List<Proxy> proxyList = defaultProxySelector.select(new URI(urls[0]));
            if (proxyList.size() > 0) {
                proxy = proxyList.get(0);
                Connect.writeToBugCheck(parent.getClass().getName() + " using proxy " + proxy.toString());
            }
            URL url = new URL(urls[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
            conn.setReadTimeout(timeoutTime);
            conn.setConnectTimeout(timeoutTime);
            conn.setDoInput(true);
            if(postMessage == null){
                conn.setRequestMethod("GET");
                conn.setDoOutput(false);
            } else{
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(postMessage.getBytes());
                os.flush();
                os.close();
            }
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            int response = conn.getResponseCode();
            Connect.writeToBugCheck(parent.getClass().getName() + "'s request " + type + " finished with code " + response);
            if (response != 200) return response;
            is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bis.mark(3000000);
            while (bis.read() != -1) {}
            bis.reset();
            return bis;
        } catch (IOException|URISyntaxException e) {
            Connect.writeToBugCheck(parent.getClass().getName() + "'s request " + type + " not finished because " + e.getMessage() + " after " + timeoutTime);
            return e;
        }
    }

    @Override
    protected void onPostExecute(Object obj) {
        parent.onTaskComplete(obj,type);
    }
}
