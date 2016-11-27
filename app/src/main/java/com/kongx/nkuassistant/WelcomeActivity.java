package com.kongx.nkuassistant;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class WelcomeActivity extends AppCompatActivity
{
    static final String DEBUG_TAG = "APP";
    class NetworkTest{
        NetworkTest(){
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Log.e("APP",networkInfo.getTypeName());
            new DownloadTest().execute("http://202.113.18.106");
        }
    }
    class DownloadTest extends AsyncTask<String,Integer,String>{
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            try {
                ProxySelector defaultProxySelector = ProxySelector.getDefault();
                Proxy proxy = null;
                List<Proxy> proxyList = defaultProxySelector.select(new URI(myurl));
                if (proxyList.size() > 0)
                {
                    proxy = proxyList.get(0);
                    Log.e(DEBUG_TAG, "Current Proxy Configuration: " + proxy.toString());
                }
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
//                conn.setReadTimeout(1000 /* milliseconds */);
//                conn.setConnectTimeout(1500 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);
                Log.e(DEBUG_TAG, "The response asadas: ");
                // Starts the query
                conn.connect();

                Log.e(DEBUG_TAG, "The response 123: ");
                int response = conn.getResponseCode();
                Log.e(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } catch (Exception e){
                e.printStackTrace();
            } finally{
                if (is != null) {
                    is.close();
                }
            }
            return null;
        }
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
        @Override
        protected String doInBackground(String... urls) {
            String str;
            try {
                Log.e("APP","Before");
                str = downloadUrl(urls[0]);
                Log.e("APP","After");
            }catch (Exception e){
                str = null;
            }
            return str;
        }

        @Override
        protected void onPostExecute(String str) {
            Log.e(DEBUG_TAG,str);

        }
    }
    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_welcome);
        System.setProperty("java.net.useSystemProxies", "true");
        new  NetworkTest();
//        GridLayout gridLayout = (GridLayout)findViewById(R.id.layout_welcome);
//        gridLayout.setPadding(0, getStatusBarHeight(), 0, 0);
//        new Timer().schedule(new TimerTask()
//        {
//            public void run()
//            {
//                Intent localIntent = new Intent(WelcomeActivity.this, IndexActivity.class);
//                WelcomeActivity.this.startActivity(localIntent);
//                WelcomeActivity.this.finish();
//            }
//        }
//                ,2500);
    }
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
