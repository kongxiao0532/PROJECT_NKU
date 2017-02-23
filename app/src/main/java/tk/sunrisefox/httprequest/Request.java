package tk.sunrisefox.httprequest;

import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Map;

public class Request{
    private static final String[] methods = { "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE" };
    private enum FollowRedirects{ UNSET, TRUE, FALSE};

    public static Request copy(Request request, Long bytes){
        Request newRequest = new Request(request);
        newRequest.startBytes = bytes;
        return newRequest;
    }

    /*package-private*/ Request(Request request){
        this.tag = request.tag;
        this.method = request.method;
        this.url = request.url;
        this.headers = request.headers;
        this.requestBody = request.requestBody;
        this.doInput = request.doInput;
        this.doOutput = request.doOutput;
        this.followRedirects = request.followRedirects;
        this.progress = request.progress;
        this.file = request.file;
        this.saveAsFile = request.saveAsFile;
        this.uiThreadCallback = request.uiThreadCallback;
        this.networkThreadCallback = request.networkThreadCallback;
        this.delay = request.delay;
    }

    private Request(Builder builder){
        this.tag = builder.tag;
        this.method = builder.method;
        this.url = builder.url;
        this.headers = builder.headers;
        this.requestBody = builder.requestBody;
        this.doInput = builder.doInput;
        this.doOutput = builder.doOutput;
        this.followRedirects = builder.followRedirects;
        this.progress = builder.progress;
        this.file = builder.file;
        this.saveAsFile = builder.saveAsFile;
        this.delay = builder.delay;
        this.exception = builder.exception;
    }
    private String tag;
    private String method;
    private String url;
    private String requestBody;
    private Map<String, String> headers;
    private boolean doInput;
    private boolean doOutput;
    private FollowRedirects followRedirects;
    private boolean saveAsFile;
    private File file;
    private IOException exception;
    private Connect.Progress progress;
    /*package-private*/ Connect connect;
    private Connect.Callback uiThreadCallback;
    private Connect.Callback networkThreadCallback;
    private int delay = 0;

    /*package-private*/ Long startBytes = 0L;
    /*package-private*/ Response response;
    /*package-private*/ void setFile(File file){
        this.file = file;
    }
    public Response response() { return this.response; }
    public String tag(){ return this.tag; }
    public String method(){ return this.method; };
    public String url() { return url; }
    public String requestBody() { return requestBody; }
    public Map<String, String> headers() { return headers; }
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
    public int delay() { return delay; }

    /*package-private*/ Connect send(){
        if(this.uiThreadCallback == null && this.networkThreadCallback == null) return null;
        connect = new Connect(this, uiThreadCallback, networkThreadCallback, progress);
        //NOTE: executing it parallel is still being tested.
        connect.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return connect;
    }

    public void send(Connect.Callback uiThreadCallback){
        connect = new Connect(this, this.uiThreadCallback = uiThreadCallback, this.networkThreadCallback = null, progress);
        //NOTE: executing it parallel is still being tested.
        connect.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public void send(Connect.Callback uiThreadCallback, Connect.Callback networkThreadCallback){
        connect = new Connect(this, this.uiThreadCallback = uiThreadCallback, this.networkThreadCallback = networkThreadCallback, progress);
        //NOTE: executing it parallel is still being tested.
        connect.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public boolean abort(){
        return connect == null || connect.cancel(true);
    }

    public boolean notifyConnect(){
        if(connect == null)return false;
        try {
            //noinspection SynchronizeOnNonFinalField
            synchronized (connect) {
                connect.notifyAll();
            }
        }catch (IllegalMonitorStateException e){
            return false;
        }
        return true;
    }

    public static class Builder{
        String tag = null;
        String method = null;
        String url = null;
        String requestBody = null;
        Map<String, String>  headers = new HashMap<>();
        Connect.Progress progress;
        File file = null;
        boolean doInput = true;
        boolean doOutput = true;
        boolean saveAsFile = false;
        FollowRedirects followRedirects = FollowRedirects.UNSET;
        IOException exception = null;
        int delay = 0;

        public Builder(){ }

        /*package-private*/ Builder(Request request){
            this.tag = request.tag;
            this.method = request.method;
            this.url = request.url;
            this.headers = request.headers;
            this.requestBody = request.requestBody;
            this.doInput = request.doInput;
            this.doOutput = request.doOutput;
            this.followRedirects = request.followRedirects;
            this.exception = request.exception;
            this.progress = request.progress;
            this.file = request.file;
            this.saveAsFile = request.saveAsFile;
            this.delay = request.delay;
        }

        public Builder tag(String tag){
            this.tag = tag;
            return this;
        }
        public Builder url(String url) {
            this.url = url;
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
            headers.put(key,value);
            return this;
        }

        public Builder setHeader(Map<String,String> headers){
            this.headers = (headers == null ? new HashMap<String, String>() : headers);
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

        public Builder delay(int delay){
            this.delay = delay;
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
