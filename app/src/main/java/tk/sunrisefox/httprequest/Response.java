package tk.sunrisefox.httprequest;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Response {


    private Request resumeRequest;
    private Connect connect = null;
    /*package-private*/ String tag;
    /*package-private*/ int code = -1;
    /*package-private*/ ByteArrayOutputStream buffer;
    /*package-private*/ Map<String, List<String>> headers;
    /*package-private*/ File file;

    /*package-private*/ Response(Connect connect){
        this.connect = connect;
    }
    /*package-private*/ void setResumeRequest(Request resumeRequest){
        this.resumeRequest = resumeRequest;
        connect = null;
    }

    /*package-private*/ void setFinished(){
        connect = null;
    }

    public boolean finished() {
        return connect == null;
    }
    public boolean canPause() {
        return file != null && !finished() && getHeader("Accept-Ranges") != null;
    }
    public boolean pause(){
        //This method shouldn't be called twice.
        if(connect != null) {
            connect.shouldPause = true;
            connect.cancel(true);
            return true;
        }
        return false;
    }
    public boolean resume(){
        //This method shouldn't be called twice.
        if(resumeRequest != null){
            connect = resumeRequest.send();
            return true;
        }
        return false;
    }
    public void abort(){
        //This method shouldn't be called twice.
        if(connect != null){
            connect.cancel(true);
            connect = null;
        }
    }

    public String tag() { return tag; }
    public int code() { return code; }
    public List<String> getHeader(String key){ return headers.get(key); }
    public byte[] bytes(){ return buffer == null ? null : buffer.toByteArray(); }
    public String body(){ return buffer == null ? null : buffer.toString(); };
    public File file(){ return file != null && file.exists() ? file : null; }
}
