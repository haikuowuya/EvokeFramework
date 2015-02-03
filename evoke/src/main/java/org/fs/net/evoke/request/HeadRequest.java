package org.fs.net.evoke.request;

import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import org.fs.net.evoke.DownloadManager;
import org.fs.net.evoke.data.HeadObject;
import org.fs.net.evoke.data.PartObject;
import org.fs.net.evoke.data.RequestObject;
import org.fs.net.evoke.listener.HeadCallback;
import org.fs.net.evoke.listener.PartCallback;
import org.fs.net.evoke.th.NamedRunnable;
import org.fs.net.evoke.util.LogUtil;
import org.fs.net.evoke.util.StringUtility;
import org.fs.net.evoke.util.Util;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    private final int connectionTimeout = 10000;

    /**
     * We have our client as static for good. 
     */
    private final static OkHttpClient client  = new OkHttpClient();
    private final static OkUrlFactory factory = new OkUrlFactory(client);
        
    private final static Pair<String, String> userAgent = new Pair<>("User-Agent", "(evoke/1.0) Android OS");
    
    private final String urlString;
    private final HeadCallback callback;
    private final File  base;    
    private final RequestObject requestObject;
    
    private long downloadedSoFar;
    private long total;
    
    private String name;
    
    private final ExecutorService         executorService;
    private final Map<Integer, Future<?>> cancelable;
     
    private final static long MAX_BUFFER = 1024 * 128;
    
    public HeadRequest(final File base, final HeadCallback callback, final RequestObject requestObject) {
        super("-H %s", requestObject.toString());
        this.urlString = requestObject.getUrlString();
        this.base = base;
        this.callback = callback;
        this.requestObject = requestObject;
        
        throwIfUrlNotValid();        
        executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 25, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), Util.threadFactory(String.format("-P %s", getName()), false));
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
    private String getName(String contentType) {
        if(name == null) {
            Uri uri = Uri.parse(urlString);
            String n = uri.getLastPathSegment();
            int ext = n.lastIndexOf('.');
            if(ext == -1) {
                ext = contentType.lastIndexOf('/');
                if(ext != -1) {
                    //append extension from contentType
                    name = String.format("%s.%s", n, contentType.substring(ext + 1));
                }
            } else {
                //it has extension in url
                name = n;
            }
        }
        return name;
    }

    /**
     * Gets file name from url string if there is.  
     * @return
     */
    private String getName() {
        return name;        
    }

    /**
     *
     * Creates part objects in 1 mb buffers. 
     * @param start start position of part. 0 default. 
     * @param headObject
     * @param length end size of download
     * @return
     */
    private List<PartObject> share(HeadObject headObject, long start, long length) {
        List<PartObject> parts = new ArrayList<>();
        if(length < 0) {
            length = headObject.getLength();
        }
        for(; start < length; start+= MAX_BUFFER) {
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
            connection.setConnectTimeout(connectionTimeout);
            connection.connect();
            int code = connection.getResponseCode();            
            if(code == 200) {
                String serverSupportsPartialDownloads = connection.getHeaderField("Accept-Ranges");
                if(!StringUtility.isNullOrEmpty(serverSupportsPartialDownloads)
                        && "bytes".equalsIgnoreCase(serverSupportsPartialDownloads)) {
                    
                    String name = getName(connection.getContentType());
                    
                    HeadObject.Builder builder = new HeadObject.Builder();
                    builder.contentType(connection.getContentType())
                           .name(name)
                           .length(connection.getContentLength())
                           .remote(urlString);
                    HeadObject headObject = builder.build();
                    
                    File file = new File(base, name);
                    if (file.exists()) {
                        boolean alreadyDownloaded = connection.getContentLength() == file.length()
                                                 || requestObject.getLimit() == file.length();
                        if (alreadyDownloaded) {
                            callback.onComplete(hashCode(), file);
                        } else {
                            
                            long limit = requestObject.getLimit() > 0 
                                    ? requestObject.getLimit() 
                                    : connection.getContentLength();
                            
                            startWithParts(share(headObject, file.length(), limit));
                            total = connection.getContentLength();
                            downloadedSoFar = file.length();
                            callback.onProgress(hashCode(), downloadedSoFar, total);
                        }
                    } else {

                        long limit = requestObject.getLimit() > 0
                                ? requestObject.getLimit()
                                : connection.getContentLength();
                        
                        startWithParts(share(headObject, 0, limit));
                        total = connection.getContentLength();
                        downloadedSoFar = 0;
                        callback.onProgress(hashCode(), downloadedSoFar, total);
                    }
                } else {
                    callback.onError(hashCode(), urlString, DownloadManager.ERROR_PARTIAL_NOT_SUPPORTED);
                }
            } else {
                callback.onError(hashCode(), urlString, DownloadManager.ERROR_SERVER_CODE);
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
                future.cancel(true);
            }
        }
        if(cleanUpSoFar) {
            File file = new File(base, getName());
            if(file.exists()) {
                file.delete();
            }
        } else {
            callback.onError(hashCode(), requestObject.getUrlString(), DownloadManager.ERROR_PART_CANCELED);
        }
    }

    /**
     * default destroy 
     */
    public void destroy() {
        destroy(false);        
    }

    /**
     * consumption method.
     * @param parts
     */
    private void startWithParts(List<PartObject> parts) {
        if (parts != null && parts.size() > 0) {
            for (PartObject part : parts) {
                PartRequest partRequest = new PartRequest(part, partCallback);
                Future<?> future = executorService.submit(partRequest);
                cancelable.put(partRequest.hashCode(), future);
            }
        }
    }

    /**
     * Callback for parts.
     */
    private final PartCallback partCallback = new PartCallback() {
        @Override
        public void onPartCompleted(long size, int id) {
            if(DownloadManager.ERROR_PART_CANCELED == size) {
                callback.onError(HeadRequest.this.hashCode(), requestObject.getUrlString(), DownloadManager.ERROR_PART_CANCELED);
            }
            else if(DownloadManager.ERROR_PART_SERVER_CODE == size) {
                callback.onError(HeadRequest.this.hashCode(), requestObject.getUrlString(), DownloadManager.ERROR_PART_SERVER_CODE);
            }
            else if(DownloadManager.ERROR_PART_UNKNOWN == size) {
                callback.onError(HeadRequest.this.hashCode(), requestObject.getUrlString(), DownloadManager.ERROR_PART_UNKNOWN);
            }
            else {
                downloadedSoFar += size;//append it.
                callback.onProgress(HeadRequest.this.hashCode(), downloadedSoFar, total);
                if (downloadedSoFar == total) {
                    File destination = requestObject.getMoveTo();
                    if(destination != null) {
                        //if the parent folder not exits then make it
                        File parent = destination.getParentFile();
                        if(!parent.exists()) {
                            parent.mkdirs();
                        }
                        File cache = new File(base, getName());
                        boolean moved = Util.move(cache, destination);
                        if(!moved) {
                            moved = cache.renameTo(destination);
                            if(!moved) {
                                throw new IllegalArgumentException("cant move file to destination.");
                            } else {
                                callback.onComplete(HeadRequest.this.hashCode(), destination);
                            }
                        }
                        else {
                            callback.onComplete(HeadRequest.this.hashCode(), destination);
                        }
                    } else {
                        callback.onComplete(HeadRequest.this.hashCode(), new File(base, getName()));
                    }
                }
            }
            //it might not be cancelled. quit it when we are done.
            Future<?> future = cancelable.remove(id);
            future.cancel(true);
        }
    };
}
