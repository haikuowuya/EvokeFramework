package org.fs.net.evoke.request;

import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import org.fs.net.evoke.DownloadManager;
import org.fs.net.evoke.data.HeadObject;
import org.fs.net.evoke.data.PartObject;
import org.fs.net.evoke.listener.HeadCallback;
import org.fs.net.evoke.listener.PartCallback;
import org.fs.net.evoke.th.NamedRunnable;
import org.fs.net.evoke.util.LogUtil;
import org.fs.net.evoke.util.StringUtility;
import org.fs.net.evoke.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Fatih on 29/01/15.
 * as org.fs.net.evoke.th.HeadRunnable
 */
public class HeadRequest extends NamedRunnable {
        
    private final static String HTTP    = "http";
    private final static String HTTPS   = "https";
    
    private final static String METHOD  = "HEAD";

    /**
     * We have our client as static for good. 
     */
    private final static OkHttpClient client  = new OkHttpClient();
    private final static OkUrlFactory factory = new OkUrlFactory(client);
        
    private final static Pair<String, String> userAgent = new Pair<>("User-Agent", "(evoke/1.0) Android OS");
    
    private final String urlString;
    private final HeadCallback callback;
    private final File  base;    
    
    private long downloadedSoFar;
    private long total;
    
    //inner concurrent part 
    private final ExecutorService         executorService;
    private final Map<Integer, Future<?>> cancelable;
     
    //buffer for 1 mb used for invoking streams 
    private final static long MAX_BUFFER = Util.pow(1024, 2);
    
    public HeadRequest(final String urlString, final File base, final HeadCallback callback) {
        super("-H %s", urlString);
        this.urlString = urlString;
        this.base = base;
        this.callback = callback;
        
        throwIfUrlNotValid();        
        executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), Util.threadFactory(String.format("-H %s Pool", getName()), false));
        cancelable = new HashMap<>();
    }

    /**
     * throws RuntimeException if the url null
     * throws IllegalArgumentException if the url scheme is none of the http or https. 
     */
    private void throwIfUrlNotValid() {
        if(StringUtility.isNullOrEmpty(urlString)) {
            throw new RuntimeException("url string is null.");
        }
        Uri uri = Uri.parse(urlString);
        if(!isScheme(uri.getScheme())) {
            throw new IllegalArgumentException("http or https scheme is only supported.");    
        }
    }

    /**
     * checks http or https in the scheme 
     * @param scheme
     * @return
     */
    private boolean isScheme(String scheme) {
        return HTTP.equalsIgnoreCase(scheme) || HTTPS.equalsIgnoreCase(scheme);            
    }

    /**
     * is log enabled. 
     * @return
     */
    protected boolean isLogEnabled() {
        return true;        
    }

    /**
     * log name 
     * @return
     */
    protected String getClassTag() {
        return HeadRequest.class.getSimpleName();
    }

    /**
     * low level log 
     * @param message
     */
    protected void log(String message) {
        log(Log.DEBUG, message);            
    }

    /**
     * Log for testing 
     * @param priority
     * @param message
     */
    protected void log(int priority, String message) {
        if(isLogEnabled()) {
            LogUtil.log(priority, getClassTag(), message);
        }
    }

    /**
     * parse Parse urlString into Valid URL object or null.
     * @return
     */
    private URL parse() {
        try {
            return new URL(urlString);
        } catch (Exception e) {
            if(isLogEnabled()) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Opens connection if URL is valid else it will return null. 
     * @return
     */
    private HttpURLConnection open() {
        URL parse = parse();
        if(parse != null) {
            return factory.open(parse());
        }
        return null;
    }

    /**
     * Gets file name from url string if there is. 
     * @return
     */
    private String getName() {
        Uri uri = Uri.parse(urlString);
        return uri.getLastPathSegment();
    }

    /**
     * Creates part objects in 1 mb buffers. 
     * @param start start position of part. 0 default. 
     * @param headObject
     * @return
     */
     private List<PartObject> share(HeadObject headObject, long start) {
        List<PartObject> parts = new ArrayList<>();
        for(; start < headObject.getLength(); start+= MAX_BUFFER) {
            PartObject.Builder builder = new PartObject.Builder();
            builder.file(new File(base, headObject.getName()))
                   .range(String.format("bytes=%d-%d", start,
                           (start + MAX_BUFFER >= headObject.getLength()
                                   ? headObject.getLength() - 1
                                   : start + MAX_BUFFER - 1)))
                   .urlString(headObject.getRemote());
            parts.add(builder.build());
        }
        return parts;
     }

    /**
     * main entry point.
     */
    @Override
    protected void execute() {
        HttpURLConnection connection = open();
        try {
            connection.setRequestMethod(METHOD);
            connection.setRequestProperty(userAgent.first, userAgent.second);
            connection.setConnectTimeout(5000);//5 secs time-out
            //connect
            connection.connect();
            int code = connection.getResponseCode();            
            if(code == 200) {
                //create data from this.
                String serverSupportsPartialDownloads = connection.getHeaderField("Accept-Ranges");//if this is null 
                if(!StringUtility.isNullOrEmpty(serverSupportsPartialDownloads)
                        && "bytes".equalsIgnoreCase(serverSupportsPartialDownloads)) {
                    //get useful parts.
                    HeadObject.Builder builder = new HeadObject.Builder();
                    builder.contentType(connection.getContentType())
                           .name(getName())
                           .length(connection.getContentLength())
                           .remote(urlString);

                    HeadObject headObject = builder.build();

                    //check if exists
                    File file = new File(base, getName());
                    if (file.exists()) {
                        boolean alreadyDownloaded = connection.getContentLength() == file.length();
                        if (alreadyDownloaded) {
                            //file already remains in the storage must have been downloaded.
                            callback.onComplete(hashCode(), file);
                        } else {
                            //resume
                            startWithParts(share(headObject, file.length()));   
                            total = connection.getContentLength();
                            downloadedSoFar = file.length();
                            callback.onProgress(hashCode(), downloadedSoFar, total);
                        }
                    } else {
                        //start new
                        startWithParts(share(headObject, 0));
                        total = connection.getContentLength();
                        downloadedSoFar = 0;
                        callback.onProgress(hashCode(), downloadedSoFar, total);
                    }
                } else {
                    //remote does not support partial download...
                    callback.onError(hashCode(), urlString, DownloadManager.ERROR_PARTIAL_NOT_SUPPORTED);
                }
            } else {
                callback.onError(hashCode(), urlString, DownloadManager.ERROR_SERVER_CODE);
                //failed notify user that this is not valid type of file we can partition on it. 
                //this is non-200 code so check it later
            }                
            connection.disconnect();
        } catch (Exception e) {
            if(isLogEnabled()) {
                e.printStackTrace();
            }
            callback.onError(hashCode(), urlString, DownloadManager.ERROR_SERVER_CODE);
        }        
    }

    /**
     * @param cleanUpSoFar
     */
    public void destroy(boolean cleanUpSoFar) {
        if(cancelable != null && cancelable.size() > 0) {
            for(int key : cancelable.keySet()) {
                Future<?> future = cancelable.remove(key);
                future.cancel(true);//interrupt these
            }
        }
        if(cleanUpSoFar) {
            File file = new File(base, getName());
            if(file.exists()) {
                file.delete();
                //this is cancel
                callback.onProgress(-1, downloadedSoFar, total);
            }
        } else {
            //this is pause
            callback.onProgress(-2, downloadedSoFar, total);
            callback.onComplete(-2, new File(base, getName()));//downloaded so far.
        }
    }

    /**
     * consumption method.
     * @param parts
     */
    private void startWithParts(List<PartObject> parts) {
        if (parts != null && parts.size() > 0) {
            for (PartObject part : parts) {
                PartRequest partRequest = new PartRequest(part, new PartCallback() {                   
                    @Override
                    public void onPartCompleted(long size, int id) {
                        cancelable.remove(id);
                        if(size == -1) {
                            //this is interrupted thread.    
                        } else {
                            //normal life cycle of thread.
                            downloadedSoFar += size;
                        }
                        callback.onProgress(HeadRequest.this.hashCode(), downloadedSoFar, total);
                        if (downloadedSoFar == total) {
                            //complete callback
                            callback.onComplete(HeadRequest.this.hashCode(), new File(base, getName()));
                        }
                    }
                });
                Future<?> future = executorService.submit(partRequest);
                cancelable.put(partRequest.hashCode(), future);
            }
        }
    }
}
