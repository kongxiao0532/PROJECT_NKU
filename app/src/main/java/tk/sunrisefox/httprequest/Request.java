package tk.sunrisefox.httprequest;

import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request{
    private static final String[] methods = { "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE" };
    private Request(Builder builder){
        this.method = builder.method;
        this.url = builder.url;
        this.headers = builder.headers;
        this.requestBody = builder.requestBody;
        this.tag = builder.tag;
        this.doInput = builder.doInput;
        this.doOutput = builder.doOutput;
        this.followRedirects = builder.followRedirects;
        this.exception = builder.exception;
    }

    private String tag;
    private String method;
    private URL url;
    private String requestBody;
    private Map<String, List<String>> headers;
    private boolean doInput;
    private boolean doOutput;
    private boolean followRedirects = false;
    private IOException exception;

    /*package-private*/ String tag(){ return this.tag; }
    public String method(){ return this.method; };
    public URL url() { return url; }
    public String requestBody() { return requestBody; }
    public Map<String, List<String>> headers() { return headers; }
    public boolean doInput() { return doInput; }
    public boolean doOutput() { return doOutput; }
    public boolean followRedirects() { return followRedirects; }
    public IOException exception() { return exception; }
    public void send(Connect.Callback uiThreadCallback){
        new Connect(this, uiThreadCallback, null).execute();
    }
    public void send(Connect.Callback uiThreadCallback, Connect.Callback networkThreadCallback){
        new Connect(this, uiThreadCallback, networkThreadCallback).execute();
    }

    public static class Builder{
        String tag = null;
        String method = null;
        URL url = null;
        String requestBody = null;
        Map<String, List<String>>  headers = new HashMap<>();
        boolean doInput = true;
        boolean doOutput = true;
        boolean followRedirects = false;

        IOException exception = null;

        public Builder(){ }
        public Builder tag(String tag){
            this.tag = tag;
            return this;
        }
        public Builder url(String url) {
            try {
                this.url = new URL(url);
            } catch (MalformedURLException e) {
                exception = e;
            }
            return this;
        }
        public Builder method(String method){
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].equals(method)) {
                    this.method = method;
                    switch (i){
                        case 0: doOutput = false; break;
                        case 2: doInput = false; break;
                    }
                    return this;
                }
            }
            exception = new ProtocolException("Method " + method + " has not been supported.");
            return this;
        }
        public Builder requestBody(String requestBody){
            this.requestBody = requestBody;
            if(method == null) method = methods[1];
            return this;
        }

        public Builder followRedirects(boolean followRedirects){
            this.followRedirects = followRedirects;
            return this;
        }

        public Builder addHeader(String key, String value){
            List<String> valueList;
            if((valueList = headers.get(key)) != null) {
                valueList = new ArrayList<>(valueList);
                valueList.add(value);
            } else {
                valueList = new ArrayList<>();
                valueList.add(value);
            }
            headers.put(key,valueList);
            return this;
        }

        public Request build(){
            if(exception == null) {
                if (tag == null) tag = "";
                if (url == null) exception = new IOException("No URL Specified");
                if (method == null) {
                    method = methods[0];
                    doOutput = false;
                }
                if (requestBody == null) requestBody = "";
                if (headers == null) headers = new HashMap<>();
            }
            return new Request(this);
        }
    }
}
