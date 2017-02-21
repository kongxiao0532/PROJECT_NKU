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
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Connect extends AsyncTask<Void, Long, Void> {
    private static final String DEBUG_TAG = "NETWORK";
    /*package-private*/ static boolean defaultFollowRedirects = false;
    private static int connectTimeout = 3000;
    private static int readTimeout = 0;
    private static String rules = null;
    private static String defaultUA = null;
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

    public static String defaultUA() {
        return defaultUA;
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

    public static void setDefaultUA(String UA) {
        defaultUA = UA;
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

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestMethod(request.method());
            connection.setDoInput(request.doInput());
            connection.setDoOutput(request.doOutput());
            connection.setInstanceFollowRedirects(request.followRedirects());
            if (defaultUA != null) connection.setRequestProperty("User-Agent", defaultUA);
            for (Map.Entry<String, List<String>> entry : request.headers().entrySet()) {
                StringBuilder builder = new StringBuilder();
                for (String value : entry.getValue()) {
                    builder.append(value);
                    builder.append(";");
                }
                builder.deleteCharAt(builder.length() - 1);
                connection.setRequestProperty(entry.getKey(), builder.toString());
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
                throw new IOException("Recovering from pause is not supported.");
            response.headers = connection.getHeaderFields();
            if (network != null) {
                //TODO: Thought is still needed on starting new thread or not
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.finished()) network.onNetworkComplete(response);
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
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[8192];

                File file = request.file();
                FileOutputStream fileOutputStream = null;
                if (startBytes == 0L) {
                    if (file != null && !file.isFile()) {
                        try {
                            if (!file.createNewFile())
                                file = null;
                        } catch (IOException e) {
                            file = null;
                        }
                    }
                    if (file == null && request.saveAsFile()) {
                        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), url.getFile());
                        for (int i = 1; file.exists(); i++)
                            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                    , url.getFile().replaceFirst("([.][^.]+)$", "_" + i + "$1"));
                        if (!file.createNewFile()) throw new IOException("No file is writable");
                    }
                    if (file != null) {
                        fileOutputStream = new FileOutputStream(file);
                        request.setFile(file);
                    }
                } else fileOutputStream = new FileOutputStream(file, true);
                String contentLength = connection.getHeaderField("Content-Length");
                long totalBytes;
                try {
                    if (contentLength == null) totalBytes = -1L;
                    else try {
                        totalBytes = Long.parseLong(contentLength);
                    } catch (NumberFormatException e) {
                        totalBytes = -1L;
                    }
                    int bytes;
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
                    if (fileOutputStream != null) fileOutputStream.write(buffer.toByteArray());
                } catch (OutOfMemoryError e) {
                    throw new IOException("OutOfMemoryError was thrown. You'd better save the response as a file.");
                }
                if (fileOutputStream != null) {
                    response.file = file;
                    fileOutputStream.close();
                } else response.buffer = buffer;

                if (network != null) {
                    response.setFinished();
                    network.onNetworkComplete(response);
                }
            }
        } catch (IOException e) {
            exception = e;
            if (network != null) {
                network.onNetworkError(e);
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
        else ui.onNetworkComplete(response);
    }

    public interface Callback {
        void onNetworkComplete(Response response);

        void onNetworkError(Exception exception);
    }

    public interface Progress {
        void updateProgress(Long current, Long total);
    }
}

