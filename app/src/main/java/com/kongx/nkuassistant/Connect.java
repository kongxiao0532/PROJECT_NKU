package com.kongx.nkuassistant;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * Created by kongx on 2016/11/27 0027.
 */

class Connect extends AsyncTask<String, Integer, BufferedInputStream> {
    private Connectable parent;
    private static final String DEBUG_TAG = "APP";
    private int type;
    public Connect(Connectable parent, int type) {
        this.parent = parent;
        this.type = type;
    }

    @Override
    protected BufferedInputStream doInBackground(String... urls) {
        InputStream is = null;
        try {
            ProxySelector defaultProxySelector = ProxySelector.getDefault();
            Proxy proxy = null;
            List<Proxy> proxyList = defaultProxySelector.select(new URI(urls[0]));
            if (proxyList.size() > 0) {
                proxy = proxyList.get(0);
                Log.e(DEBUG_TAG, "Current Proxy Configuration: " + proxy.toString());
            }
            URL url = new URL(urls[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
            conn.setReadTimeout(1000);
            conn.setConnectTimeout(1500);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.connect();
            int response = conn.getResponseCode();
            Log.e(DEBUG_TAG, "Response Code: " + response);
            if (response != 200) return null;
            is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bis.mark(3000000);
            while (bis.read() != -1);
            bis.reset();
            return bis;
        } catch (IOException|URISyntaxException e) {
            Log.e(DEBUG_TAG, e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(BufferedInputStream is) {
        parent.onTaskComplete(is,type);
    }
}
