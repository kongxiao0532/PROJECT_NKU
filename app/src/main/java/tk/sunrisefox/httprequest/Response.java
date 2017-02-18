package tk.sunrisefox.httprequest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Response {
    /*package-private*/ String tag;
    /*package-private*/ int code = -1;
    /*package-private*/ BufferedInputStream bufferedStream;
    /*package-private*/ ByteArrayOutputStream buffer;
    /*package-private*/ Map<String, List<String>> headers;
    /*package-private*/ Response(){

    }

    public String tag() { return tag; }
    public int code() { return code; }
    public List<String> getHeader(String key){ return headers.get(key); }
    public byte[] bytes(){ return buffer.toByteArray(); }
    public String body(){ return buffer.toString(); };
}
