package tk.sunrisefox.httprequest;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Connect extends AsyncTask<Void, Long, Void> {
    private static final String DEBUG_TAG = "NETWORK";
    /*package-private*/ static boolean defaultFollowRedirects = false;
    private static int connectTimeout = 3000;
    private static int readTimeout = 0;
    private static String rules = null;
    private static Map<String,String> defaultHeaders = new HashMap<>();
    private static File defaultSavingDirectory;
    private static void resetDefaultSavingDirectory(){
        try {
            defaultSavingDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        }catch (Throwable throwable){
            defaultSavingDirectory = null;
        }
    }
    static {
        resetDefaultSavingDirectory();
    }
    final /*package-private*/ Request request;
    final private Callback ui;
    final private Callback network;
    final private Progress progress;
    /*package-private*/ boolean shouldPause = false;
    private Exception exception = null;
    private Response response;
    private Long startBytes = 0L;
    private Long finishedBytes = 0L;

    /*package-private*/ Connect(Request request, Callback ui, Callback network, Progress progress) {
        this.request = request;
        this.ui = ui;
        this.network = network;
        this.progress = progress;
    }

    public static void initialize(CookieManager cookieManager) {
        CookieHandler.setDefault(cookieManager);
    }

    public static int connectTimeout() {
        return connectTimeout;
    }

    public static int readTimeout() {
        return readTimeout;
    }

    public static boolean defaultFollowRedirects() {
        return defaultFollowRedirects;
    }

    public static void setConnectTimeout(int time) {
        connectTimeout = (time < 0 ? 0 : time);
    }

    public static void setReadTimeout(int time) {
        readTimeout = (time < 0 ? 0 : time);
    }

    public static void setDefaultFollowRedirects(boolean followRedirects) {
        defaultFollowRedirects = followRedirects;
    }

    public static boolean setDefaultSavingDirectory(File file) {
        if(file == null) { resetDefaultSavingDirectory(); return true; }
        if(file.isDirectory()) { defaultSavingDirectory = file; return true; }
        return false;
    }

    public static Map<String,String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public static void setDefaultHeaders(Map<String,String> defaultHeaders) {
        if(defaultHeaders == null) defaultHeaders = new HashMap<>();
        Connect.defaultHeaders = defaultHeaders;
    }
    public static void disableHttpsCertVerification (boolean enable) {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
    public static void addDefaultHeaders(String key, String value) {
        defaultHeaders.put(key, value);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean setDefaultReplaceRules(String rules) {
        if (rules != null) {
            // ^http://(eamis.nankai.edu.cn)<>https://221.238.246.69/web/1/http/0/$1<>all ([.][^.]+)$<>3$1
            String[] rule = rules.split(" ");
            try {
                for (String r : rule) {
                    String[] p = r.split("<>");
                    Pattern.compile(p[0]);
                    Pattern.compile(p[1]);
                }
            } catch (Exception e) {
                return false;
            }
        }
        Connect.rules = rules;
        return true;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            //noinspection ThrowableResultOfMethodCallIgnored
            if (request.exception() != null) throw request.exception();
            String urlString = request.url();
            if (rules != null) {
                String[] rule = rules.split(" ");
                for (String r : rule) {
                    String[] p = r.split("<>");
                    urlString = (p.length == 3 && p[2].equals("all") ? urlString.replaceAll(p[0].trim(), p[1].trim()) : urlString.replaceFirst(p[0].trim(), p[1].trim()));
                }
            }

            if(request.delay()!=0){
                synchronized (this){
                    try {
                        if(request.delay() == -1) wait();
                        else wait(request.delay());
                    }catch (InterruptedException e){
                        Log.e("Network",Log.getStackTraceString(e));
                    }
                }
            }
            request.connect = null;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestMethod(request.method());
            connection.setDoInput(request.doInput());
            connection.setDoOutput(request.doOutput());
            connection.setInstanceFollowRedirects(request.followRedirects());
            for (Map.Entry<String, String> entry : defaultHeaders.entrySet()){
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String> entry : request.headers().entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            if ((startBytes = finishedBytes = request.startBytes) != 0) {
                connection.setRequestProperty("Range", "bytes=" + String.valueOf(startBytes) + "-");
            }
            if (request.doOutput()) {
                OutputStream os = connection.getOutputStream();
                os.write(request.requestBody().getBytes());
                os.flush();
                os.close();
            }
            connection.connect();
            request.response = response = new Response(this);
            response.tag = request.tag();
            response.code = connection.getResponseCode();
            if (startBytes != 0L && response.code() != 206)
                throw new IOException("Recovering from pause is not possible.");
            response.headers = connection.getHeaderFields();
            if (network != null) {
                //TODO: Thought is still needed on whether to start a new thread or not.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (response != null && !response.finished()) network.onNetworkComplete(response);
                    }
                }).start();
            }

            InputStream stream = null;
            if (request.doInput()) {
                try {
                    stream = connection.getInputStream();
                } catch (FileNotFoundException e) {
                    stream = connection.getErrorStream();
                }
            }
            if (stream != null) {
                long downloadedBytes = 0;
                String contentLength = connection.getHeaderField("Content-Length");
                long totalBytes;
                if (contentLength == null) totalBytes = -1L;
                else try {
                    totalBytes = Long.parseLong(contentLength);
                } catch (NumberFormatException e) {
                    totalBytes = -1L;
                }

                File file = request.file();
                FileOutputStream fileOutputStream = null;
                if (startBytes == 0L) {
                    if (file != null && !file.isFile()) {
                        try {
                            if(file.isDirectory()){
                                for (int i = 1; file.exists(); i++)
                                    file = new File(file
                                            , url.getFile().replaceFirst("([.][^.]+)$", "_" + i + "$1"));
                            }else if (!file.createNewFile())
                                file = null;
                        } catch (IOException e) {
                            file = null;
                        }
                    }
                    if (file == null && request.saveAsFile()) {
                        file = new File(defaultSavingDirectory, url.getFile());
                        for (int i = 1; file.exists(); i++)
                            file = new File(defaultSavingDirectory
                                    , url.getFile().replaceFirst("([.][^.]+)$", "_" + i + "$1"));
                        if (!file.createNewFile()) throw new IOException("No file is writable");
                    }
                    if (file != null) {
                        fileOutputStream = new FileOutputStream(file);
                        request.setFile(file);
                    }
                } else fileOutputStream = new FileOutputStream(file, true);
                ByteArrayOutputStream buffer;
                try {
                    if(totalBytes == -1L || fileOutputStream != null){
                        buffer = new ByteArrayOutputStream();
                    }else buffer = new ByteArrayOutputStream(((int) totalBytes + 1024));
                    int nRead;
                    byte[] data = new byte[8192];
                    int bytes;
                    try {
                        while ((nRead = stream.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                            downloadedBytes += nRead;
                            if (progress != null) publishProgress(downloadedBytes, totalBytes);
                            if (fileOutputStream != null && (bytes = buffer.size()) >= 0x100000) {
                                fileOutputStream.write(buffer.toByteArray());
                                fileOutputStream.flush();
                                finishedBytes += bytes;
                                buffer.reset();
                                if (isCancelled()) {
                                    stream.close();
                                    break;
                                }
                            }
                        }
                    }catch (ProtocolException e){
                        exception = e;
                    }
                    if (fileOutputStream != null) fileOutputStream.write(buffer.toByteArray());
                } catch (OutOfMemoryError e) {
                    throw new IOException("OutOfMemoryError was thrown. You'd better save the response as a file.");
                }
                if (fileOutputStream != null) {
                    response.file = file;
                    fileOutputStream.close();
                } else response.buffer = buffer;

                if (network != null)
                    network.onNetworkComplete(response);
            }
        } catch (IOException e) {
            exception = e;
            response = null;
        }finally {
            request.connect = null;
            if(response != null)
                response.connect = null;
            if(exception != null && network != null){
                    network.onNetworkError(exception);
            }
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        if (shouldPause && response != null) {
            response.setResumeRequest(Request.copy(request, finishedBytes));
        } else {
            IOException exception = new IOException("User Cancelled.");
            if (ui != null) ui.onNetworkError(exception);
            if (network != null) network.onNetworkError(exception);
        }
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        progress.updateProgress(startBytes + values[0], startBytes + values[1]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (ui == null) return;
        if (exception != null) ui.onNetworkError(exception);
        if (response != null) ui.onNetworkComplete(response);
    }

    public interface Callback {
        void onNetworkComplete(Response response);

        void onNetworkError(Exception exception);
    }

    public interface Progress {
        void updateProgress(Long current, Long total);
    }
}

