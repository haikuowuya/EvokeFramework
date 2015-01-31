package org.fs.net.evoke;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.fs.net.evoke.listener.HeadCallback;
import org.fs.net.evoke.request.HeadRequest;
import org.fs.net.evoke.util.Util;

import java.io.File;
import java.lang.ref.WeakReference;
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
 * Created by Fatih on 30/01/15.
 * as org.fs.net.evoke.DownloadManager
 */
public final class DownloadManager {
    
    public final static String           DOWNLOAD_START_ACTION    = "org.fs.net.evoke.action.DOWNLOAD_START";
    public final static String           DOWNLOAD_FINISH_ACTION   = "org.fs.net.evoke.action.DOWNLOAD_FINISH";
    public final static String           DOWNLOAD_PROGRESS_ACTION = "org.fs.net.evoke.action.DOWNLOAD_PROGRESS";
    public final static String           DOWNLOAD_PAUSE_ACTION    = "org.fs.net.evoke.action.DOWNLOAD_PAUSE";
    public final static String           DOWNLOAD_RESUME_ACTION   = "org.fs.net.evoke.action.DOWNLOAD_RESUME";

    public final static String           DOWNLOAD_ID              = "id";
    
    public final static int              ERROR_SERVER_CODE           = 0 << 1;
    public final static int              ERROR_PARTIAL_NOT_SUPPORTED = 1 << 1;
    
    private final WeakReference<Context> contextRef;  
    private final static String          DOWNLOAD_FOLDER_NAME = "evoke";
    
    private static DownloadManager       instance = null;
    
    private final File                      downloadFolder;
    private final ExecutorService           executorService;
    private final Map<Integer, HeadRequest> taskPool;
    
    private final List<String>              currentPool;
    
    private DownloadManager(Context context) {
        //for security pall
        if(context == null) {
            throw new IllegalArgumentException("Context can not be null.");
        }
        
        downloadFolder = new File(context.getFilesDir(), DOWNLOAD_FOLDER_NAME);
        if(!downloadFolder.exists()) {
            downloadFolder.mkdirs();
        }
        //background thread pool to do these operations in.
        executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(), Util.threadFactory("Evoke Pool", false));
        //in case user might want to cancel
        taskPool = new HashMap<>();
        //we store currently downloaded in here
        currentPool = new ArrayList<>();
        //reference the context       
        contextRef = new WeakReference<>(context);
    }

    /**
     * Only way to create or get instance of download manager. 
     * @param context
     * @return
     */
    public static DownloadManager getInstance(Context context) {
        if(instance == null) {
            instance = new DownloadManager(context);
        }
        return instance;
    }
    
    //Control connection CELL OR WIFI if wifi don't hesitate else think about it.
    public void stop(int id) {
        //stop and drop download             
    }
    
    public void start(Uri uri) {
        //before adding check if its already there or not..
        //start and fresh download
        HeadRequest request = new HeadRequest(uri.toString(), downloadFolder, callback);
        int id = request.hashCode();
        taskPool.put(id, request);
        
        //pool it.
        executorService.execute(request);
    }
    
    public void pause() {
        //pause an non-complete download        
    }
    
    public void resume() {
        //resume an non-complete download            
    }
    
    private final HeadCallback callback = new HeadCallback() {
        @Override
        public void onProgress(int id, long soFarDownloaded, long total) {
            Log.println(Log.ERROR, "DownloadManager", String.format("id: %d - downloaded: %d - total: %d", id, soFarDownloaded, total));
        }

        @Override
        public void onComplete(int id, File file) {
            taskPool.remove(id);
            Log.println(Log.ERROR, "DownloadManager", String.format("id: %d - file: %s", id, file.toURI().toString()));
        }

        @Override
        public void onError(int id, String url, int reason) {
            taskPool.remove(id);
            Log.println(Log.ERROR, "DownloadManager", String.format("id: %d - url: %s - reason: %d", id, url, reason));
        }
    };
    
    
}
