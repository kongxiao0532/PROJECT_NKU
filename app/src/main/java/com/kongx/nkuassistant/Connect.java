package com.kongx.nkuassistant;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.lang.String;
import java.util.Objects;

/**
 * Created by kongx on 2016/11/27 0027.
 */

//public class Connect {
//        Connect() {
//            ConnectivityManager connMgr = (ConnectivityManager)
//                    getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//            if (networkInfo == null) {
//                IndexActivity.this.connectionErrorToast.show();
//                return;
//            }
//            Log.e("APP", networkInfo.getTypeName());
//            new Connect.ConnectionTest().execute("http://222.30.49.10");
//        }
class Connect extends AsyncTask<String, Integer, InputStream> {
    private Connectable parent;
    static final String DEBUG_TAG = "APP";

    public Connect(Connectable parent) {
        this.parent = parent;
    }

    private InputStream downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 10000;

        try {
            ProxySelector defaultProxySelector = ProxySelector.getDefault();
            Proxy proxy = null;
            List<Proxy> proxyList = defaultProxySelector.select(new URI(myurl));
            if (proxyList.size() > 0) {
                proxy = proxyList.get(0);
                Log.e(DEBUG_TAG, "Current Proxy Configuration: " + proxy.toString());
            }
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
            conn.setReadTimeout(1000 /* milliseconds */);
            conn.setConnectTimeout(1500 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(false);
//                Log.e(DEBUG_TAG, "The response asadas: ");
            // Starts the query
            conn.connect();

//                Log.e(DEBUG_TAG, "The response 123: ");
            int response = conn.getResponseCode();
            Log.e(DEBUG_TAG, "Response Code: " + response);
            if (response != 200) return null;
            is = conn.getInputStream();
//                // Convert the InputStream into a string
//                String contentAsString = readIt(is, len);
            return is;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
//        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
//            Reader reader = null;
//            reader = new InputStreamReader(stream, "UTF-8");
//            char[] buffer = new char[len];
//            reader.read(buffer);
//            return new String(buffer);
//        }

    @Override
    protected InputStream doInBackground(String... urls) {
        String str;
        InputStream is;
        try {
            Log.e("APP", "Before");
            is = downloadUrl(urls[0]);
            Log.e("APP", "After");
        } catch (Exception e) {
            is = null;
        }
        return is;
    }

    @Override
    protected void onPostExecute(InputStream is) {
//            Log.e(DEBUG_TAG, str);
        parent.updateConnect(is);

    }
}
