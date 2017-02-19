package tk.sunrisefox.httprequest;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

public class Connect extends AsyncTask<Void, Long, Void> {
    private static final String DEBUG_TAG = "NETWORK";
    private static int connectTimeout = 3000;
    private static int readTimeout = 0;
    private static String defaultUA = null;
    private Exception exception = null;
    private Request request;
    private Response response;
    private Callback ui;
    private Callback network;
    private Progress progress;

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

    public static void setConnectTimeout(int time) {
        connectTimeout = (time < 0 ? 0 : time);
    }

    public static void setReadTimeout(int time) {
        readTimeout = (time < 0 ? 0 : time);
    }

    public static void setDefaultUA(String UA) {
        defaultUA = UA;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            //noinspection ThrowableResultOfMethodCallIgnored
            if (request.exception() != null) throw request.exception();
            URL url = request.url();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(0);
            connection.setReadTimeout(0);
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

            if (request.doOutput()) {
                OutputStream os = connection.getOutputStream();
                os.write(request.requestBody().getBytes());
                os.flush();
                os.close();
            }
            connection.connect();
            response = new Response();
            response.code = connection.getResponseCode();
            response.headers = connection.getHeaderFields();
            response.tag = request.tag();
            if (request.doInput()) {
                long downloadedBytes = 0;
                InputStream stream = connection.getInputStream();

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[8192];

                File file = request.file();
                FileOutputStream fileOutputStream = null;
                if (file != null && !file.isFile()) {
                    try {
                        if (!file.createNewFile())
                            file = null;
                    } catch (IOException e) {
                        file = null;
                    }
                }
                if (file == null && request.saveAsFile()) {
                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), request.url().getFile());
                    for (int i = 1; file.exists(); i++)
                        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                , request.url().getFile().replaceFirst("([.][^.]+)$", "_" + i + "$1"));
                    if (!file.createNewFile()) throw new IOException("No file is writable");
                }
                if (file != null) fileOutputStream = new FileOutputStream(file);
                String contentLength = connection.getHeaderField("Content-Length");
                long totalBytes;
                try {
                    if (contentLength == null) totalBytes = -1L;
                    else try {
                        totalBytes = Long.parseLong(contentLength);
                    } catch (NumberFormatException e) {
                        totalBytes = -1L;
                    }
                    while ((nRead = stream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                        downloadedBytes += nRead;
                        if (progress != null) publishProgress(downloadedBytes, totalBytes);
                        if (fileOutputStream != null && buffer.size() >= 0x500000) {
                            fileOutputStream.write(buffer.toByteArray());
                            fileOutputStream.flush();
                            buffer.reset();
                        }
                    }
                    if (fileOutputStream != null) fileOutputStream.write(buffer.toByteArray());
                } catch (OutOfMemoryError e) {
                    throw new IOException("OutOfMemoryError was thrown. You'd better save the response as a file.");
                }
                if (fileOutputStream != null) response.file = file;
                else response.buffer = buffer;
            }
            if (network != null)
                network.onNetworkComplete(response);
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
        IOException exception = new IOException("User Cancelled.");
        if (ui != null) ui.onNetworkError(exception);
        if (network != null) network.onNetworkError(exception);
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        progress.updateProgress(values[0], values[1]);
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

