package org.fs.net.evoke.request;

import android.util.Log;
import android.util.Pair;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import org.fs.net.evoke.DownloadManager;
import org.fs.net.evoke.data.PartObject;
import org.fs.net.evoke.listener.PartCallback;
import org.fs.net.evoke.th.NamedRunnable;
import org.fs.net.evoke.util.LogUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Fatih on 29/01/15.
 * as org.fs.net.evoke.request.PartRequest
 */
public class PartRequest extends NamedRunnable {

    /**
     *  this should be executed in 60 secs.
     */
    
    private final PartObject part;
    private final PartCallback callback;

    private final int connectionTimeout = 10000;
    private final int readTimeout       = 25000;
    
    private final static OkHttpClient client  = new OkHttpClient();
    private final static OkUrlFactory factory = new OkUrlFactory(client);

    private final static Pair<String, String> userAgent = new Pair<>("User-Agent", "(evoke/1.0) Android OS");
    
    private final static int MAX_BUFFER = 1024 * 1024;
    
    private final static String METHOD = "GET";
    private final static String RANGES = "Range";
    
    public PartRequest(final PartObject part, final PartCallback callback) {
        super("-P %s", part.toString());
        this.part = part;
        this.callback = callback;
    }

    /**
     *
     * @return
     */
    protected boolean isLogEnabled() {
        return true;
    }

    /**
     *
     * @return
     */
    protected String getClassTag() {
        return HeadRequest.class.getSimpleName();
    }

    /**
     *
     * @param message
     */
    protected void log(String message) {
        log(Log.DEBUG, message);
    }

    /**
     *
     * @param priority
     * @param message
     */
    protected void log(int priority, String message) {
        if(isLogEnabled()) {
            LogUtil.log(priority, getClassTag(), message);
        }
    }

    /**
     * parse 
     * @return
     */
    private URL parse() {
        try {
            return new URL(part.getUrlString());
        } catch (Exception e) {
            if(isLogEnabled()) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * 
     * @return
     */
    private HttpURLConnection open() {
        URL parse = parse();
        if(parse != null) {
            return factory.open(parse());
        }
        return null;
    }
    
    @Override
    protected void execute() {
        if(Thread.interrupted()) {
            callback.onPartCompleted(DownloadManager.ERROR_PART_CANCELED, hashCode());
            return;
        }
        HttpURLConnection connection = open();
        try {
            connection.setRequestMethod(METHOD);
            connection.setRequestProperty(userAgent.first, userAgent.second);
            connection.setRequestProperty(RANGES, part.getRange());
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(readTimeout);
            connection.connect();
            int code = connection.getResponseCode();
            if(code == 206) {
                FileOutputStream fos = part.getFile().length() == 0
                                            ? new FileOutputStream(part.getFile())
                                            : new FileOutputStream(part.getFile(), true);
                
                InputStream is = new BufferedInputStream(connection.getInputStream());
                BufferedOutputStream out = new BufferedOutputStream(fos, MAX_BUFFER);
                byte[] buffer = new byte[MAX_BUFFER];
                int size;
                while((size = is.read(buffer)) >= 0) {
                    out.write(buffer, 0, size);
                }
                out.close();
                is.close();
                fos.flush();
                fos.close();
                callback.onPartCompleted(connection.getContentLength(), hashCode());
            }
            else {
                callback.onPartCompleted(DownloadManager.ERROR_PART_SERVER_CODE, hashCode());
            }
            connection.disconnect();
        } catch (Exception exp) {
            if(isLogEnabled()) {
                exp.printStackTrace();
            }
            callback.onPartCompleted(DownloadManager.ERROR_PART_UNKNOWN, hashCode());
        }
    }
}
