package tk.sunrisefox.httprequest;

import android.util.Log;

import java.io.File;
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
    private enum FollowRedirects{ UNSET, TRUE, FALSE};

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
        this.progress = builder.progress;
        this.file = builder.file;
        this.saveAsFile = builder.saveAsFile;
    }
    private String tag;
    private String method;
    private URL url;
    private String requestBody;
    private Map<String, List<String>> headers;
    private boolean doInput;
    private boolean doOutput;
    private FollowRedirects followRedirects;
    private boolean saveAsFile;
    private File file;
    private IOException exception;
    private Connect.Progress progress;
    private Connect connect;
    /*package-private*/ Response response;

    public Response response() { return this.response; }
    public String tag(){ return this.tag; }
    public String method(){ return this.method; };
    public URL url() { return url; }
    public String requestBody() { return requestBody; }
    public Map<String, List<String>> headers() { return headers; }
    public boolean doInput() { return doInput; }
    public boolean doOutput() { return doOutput; }
    public boolean followRedirects() {
        switch (followRedirects){
            case TRUE: return true;
            case FALSE: return false;
            default:break;
        }
        return Connect.defaultFollowRedirects;
    }
    public boolean saveAsFile() { return saveAsFile; }
    public File file() { return file; }
    public IOException exception() { return exception; }
    public void send(Connect.Callback uiThreadCallback){
        connect = new Connect(this, uiThreadCallback, null, progress);
        connect.execute();
    }
    public void send(Connect.Callback uiThreadCallback, Connect.Callback networkThreadCallback){
        connect = new Connect(this, uiThreadCallback, networkThreadCallback, progress);
        connect.execute();
    }
    public boolean abort(){
        return connect == null || connect.cancel(true);
    }
    public static class Builder{
        String tag = null;
        String method = null;
        URL url = null;
        String requestBody = null;
        Map<String, List<String>>  headers = new HashMap<>();
        Connect.Progress progress;
        File file = null;
        boolean doInput = true;
        boolean doOutput = true;
        boolean saveAsFile = false;
        FollowRedirects followRedirects = FollowRedirects.UNSET;
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
                        case 2: doOutput = false; doInput = false; break;
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
            this.followRedirects = (followRedirects ? FollowRedirects.TRUE : FollowRedirects.FALSE);
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

        public Builder saveAsFile(){
            this.saveAsFile = true;
            if(followRedirects == FollowRedirects.UNSET) this.followRedirects = FollowRedirects.TRUE;
            return this;
        }

        public Builder saveAsFile(File file){
            this.file = file;
            this.saveAsFile = true;
            if(followRedirects == FollowRedirects.UNSET) this.followRedirects = FollowRedirects.TRUE;
            return this;
        }

        public Builder progress(Connect.Progress progress){
            this.progress = progress;
            return this;
        }

        public Request head(Connect.Callback uiThreadCallback){
            method("HEAD");
            Request request = build();
            request.send(uiThreadCallback);
            return request;
        }

        public Request head(Connect.Callback uiThreadCallback, Connect.Callback networkThreadCallback){
            method("HEAD");
            Request request = build();
            request.send(uiThreadCallback, networkThreadCallback);
            return request;
        }

        public Request get(Connect.Callback uiThreadCallback){
            method("GET");
            Request request = build();
            request.send(uiThreadCallback);
            return request;
        }

        public Request get(Connect.Callback uiThreadCallback, Connect.Callback networkThreadCallback){
            method("GET");
            Request request = build();
            request.send(uiThreadCallback, networkThreadCallback);
            return request;
        }

        public Request post(Connect.Callback uiThreadCallback){
            method("POST");
            Request request = build();
            request.send(uiThreadCallback);
            return request;
        }

        public Request post(String requestBody, Connect.Callback uiThreadCallback){
            method("POST");
            requestBody(requestBody);
            Request request = build();
            request.send(uiThreadCallback);
            return request;
        }

        public Request post(Connect.Callback uiThreadCallback, Connect.Callback networkThreadCallback){
            method("POST");
            Request request = build();
            request.send(uiThreadCallback, networkThreadCallback);
            return request;
        }

        public Request post(String requestBody, Connect.Callback uiThreadCallback, Connect.Callback networkThreadCallback){
            method("POST");
            requestBody(requestBody);
            Request request = build();
            request.send(uiThreadCallback, networkThreadCallback);
            return request;
        }

        public Request build(){
            if(exception == null) {
                if (tag == null) tag = "";
                if (url == null) exception = new IOException("No URL Specified");
                if (method == null) method("GET");
                if (requestBody == null) requestBody = "";
                if (headers == null) headers = new HashMap<>();
            }
            return new Request(this);
        }
    }
}
