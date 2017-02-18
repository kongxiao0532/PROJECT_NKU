package tk.sunrisefox.httprequest;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Connect extends AsyncTask<Void, Void, Void> {
    public interface Callback {
        void onNetworkComplete(Response response);
        void onNetworkError(Exception exception);
    }


    private static final String DEBUG_TAG = "NETWORK";

    public static void initialize(CookieManager cookieManager) {
        CookieHandler.setDefault(cookieManager);
    }

    private static int timeout = 3000;
    public static int timeout() { return timeout; }
    public static void serTimeout(int time) {
        timeout = time;
    }


    private boolean error = false;
    private Request request;
    private Response response;
    private Callback ui;
    private Callback network;

    /*package-private*/ Connect(Request request, Callback ui, Callback network) {
        this.request = request;
        this.ui = ui;
        this.network = network;
    }

    @Override
    protected Void doInBackground(Void... params) {
//            ProxySelector defaultProxySelector = ProxySelector.getDefault();
//            Proxy proxy = null;
//            List<Proxy> proxyList = defaultProxySelector.select(new URI(urls[0]));
//            if (proxyList.size() > 0) proxy = proxyList.get(0);
        try {
            if(request.exception() != null) throw request.exception();
            URL url = request.url();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout);

            connection.setDoInput(true);
            connection.setRequestMethod(request.method());
            connection.setDoInput(request.doInput());
            connection.setDoOutput(request.doOutput());
            connection.setInstanceFollowRedirects(request.followRedirects());
            for(Map.Entry<String, List<String>> entry : request.headers().entrySet()){
                StringBuilder builder = new StringBuilder();
                for(String value:entry.getValue()){
                    builder.append(value);
                    builder.append(";");
                }
                builder.deleteCharAt(builder.length()-1);
                connection.setRequestProperty(entry.getKey(),builder.toString());
            }

            if(request.doOutput()){
                OutputStream os = connection.getOutputStream();
                os.write(request.requestBody().getBytes());
                os.flush();
                os.close();
            }
            connection.connect();

            InputStream stream = connection.getInputStream();
            response = new Response();
            response.code = connection.getResponseCode();
            response.headers = connection.getHeaderFields();
            response.tag = request.tag();

            BufferedInputStream bis = new BufferedInputStream(stream);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[8192];

            //TODO: notice how many contents are read
            String contentLength = connection.getHeaderField("Content-Length");
            if(contentLength != null){
                try{
                    int length = Integer.parseInt(contentLength);
                    if(length < 0) throw new NumberFormatException();
                    bis.mark(length);
                    while ((nRead = bis.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    bis.reset();
                    response.bufferedStream = bis;
                }catch (NumberFormatException e){
                    while ((nRead = bis.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                }
            } else{
                while ((nRead = bis.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
            }
            buffer.flush();
            response.buffer = buffer;
            if(network != null)
                network.onNetworkComplete(response);
        } catch (IOException e) {
            error = true;
            if(network != null) {
                network.onNetworkError(e);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(!error && ui != null)
            ui.onNetworkComplete(response);
    }
}

