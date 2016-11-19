package com.kongx.nkuassistant;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by kongx on 2016/11/17 0017.
 */

public class Login {
    public static void sd() throws Exception{
        URL url = new URL("www.baidu.com");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

    }
}
