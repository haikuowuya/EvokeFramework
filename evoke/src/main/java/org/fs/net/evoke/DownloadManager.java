package org.fs.net.evoke;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import org.fs.net.evoke.data.RequestObject;
import org.fs.net.evoke.database.DatabaseHelper;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Fatih on 30/01/15.
 * as org.fs.net.evoke.DownloadManager
 */
public final class DownloadManager {
    
    public final static String           ACTION_PROGRESS             = "org.fs.net.evoke.action.PROGRESS";
    public final static String           ACTION_COMPLETE             = "org.fs.net.evoke.action.COMPLETE";
    public final static String           ACTION_ERROR                = "org.fs.net.evoke.action.ERROR";
    
    public final static int              ERROR_SERVER_CODE           = 0 << 1;
    public final static int              ERROR_PARTIAL_NOT_SUPPORTED = 1 << 1;
    
    public final static int              ERROR_PART_SERVER_CODE      = 2 << 1;
    public final static int              ERROR_PART_CANCELED         = 3 << 1;
    public final static int              ERROR_PART_UNKNOWN          = 4 << 1;
    
    private final WeakReference<Context> contextRef;  
    private final static String          DOWNLOAD_FOLDER_NAME = "evoke";
    
    private static DownloadManager       instance = null;
    
    private final File                      downloadFolder;
    private final ExecutorService           executorService;
    private final Map<Integer, HeadRequest> taskPool;
    
    private final List<RequestObject>       waitingPool;
    private final Map<Integer, HeadRequest> resumePool; 
    
    private final DatabaseHelper            databaseHelper;
    
    private DownloadManager(Context context) {
        if(context == null) {
            throw new IllegalArgumentException("Context can not be null.");
        }
        databaseHelper = DatabaseHelper.getInstance(context);
        downloadFolder = new File(context.getFilesDir(), DOWNLOAD_FOLDER_NAME);
        if(!downloadFolder.exists()) {
            downloadFolder.mkdirs();
        }
        executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(), Util.threadFactory("Evoke Pool", false));
        taskPool = new HashMap<>();
        resumePool = new HashMap<>();
        waitingPool = new ArrayList<>();
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

    /**
     * Stops if task is on the run mode and deletes all downloaded data so far. 
     * @param id
     */
    public void stop(int id) {
        if(taskPool.containsKey(id)) {
            HeadRequest request = taskPool.remove(id);
            request.destroy(true);
        }
    }

    /**
     * Start request
     * if pool is ok then start running
     * else 
     * @param requestObject
     */
    public void start(RequestObject requestObject) {
        if(taskPool.size() > 5) {
            waitingPool.add(requestObject);
        }
        else {
            HeadRequest request = new HeadRequest(downloadFolder, headCallback, requestObject);
            int id = request.hashCode();
            taskPool.put(id, request);
            executorService.execute(request);
        }
    }

    /**
     * pause running task and keep downloaded data 
     * @param id
     */
    public void pause(int id) {
        if(taskPool.containsKey(id)) {
            HeadRequest request = taskPool.remove(id);            
            request.destroy();
            resumePool.put(id, request);
        }        
    }

    /**
     * resumes paused task only
     * @param id
     */
    public void resume(int id) {
        if(resumePool.containsKey(id)) {
            HeadRequest request = resumePool.remove(id);
            taskPool.put(id, request);
            executorService.execute(request);
        }
    }
    
    private void removeAndEnqueue(int id) {
        taskPool.remove(id);
        if(waitingPool.size() > 0 && !(taskPool.size() > 5)) {
            RequestObject requestObject = waitingPool.remove(0);
            HeadRequest request = new HeadRequest(downloadFolder, headCallback, requestObject);
            int newId = request.hashCode();
            taskPool.put(newId, request);
            executorService.execute(request);
        }        
    }
    
    private Uri toUri(File file) {
        if(file == null) {
            return null;
        }
        String urlString = file.toURI().toString();
        return Uri.parse(urlString);
    }
    
    private void sendBroadcast(Bundle data, String action) {
        Intent intent = new Intent(action);
        intent.putExtras(data);
        Context context = contextRef.get();
        if(context != null) {
            context.sendBroadcast(intent);
        }
    }
    
    public final static String EXTRA_ID                = "id";
    public final static String EXTRA_DOWNLOADED_SO_FAR = "downloaded.so.far";
    public final static String EXTRA_TOTAL             = "total";
    public final static String EXTRA_FILE_URL          = "uri";
    public final static String EXTRA_REMOTE_URL        = "remote";
    public final static String EXTRA_ERROR             = "error";
    
    private final HeadCallback headCallback = new HeadCallback() {
        
        @Override
        public void onProgress(int id, long soFarDownloaded, long total) {
            Bundle data = new Bundle();
            data.putInt(EXTRA_ID, id);
            data.putLong(EXTRA_DOWNLOADED_SO_FAR, soFarDownloaded);
            data.putLong(EXTRA_TOTAL, total);
            sendBroadcast(data, ACTION_PROGRESS);
        }

        @Override
        public void onComplete(int id, File file) {
            removeAndEnqueue(id);
            Bundle data = new Bundle();
            data.putInt(EXTRA_ID, id);
            data.putString(EXTRA_FILE_URL, toUri(file).toString());
            sendBroadcast(data, ACTION_COMPLETE);
        }

        @Override
        public void onError(int id, String urlString, int reason) {
            removeAndEnqueue(id);
            Bundle data = new Bundle();
            data.putInt(EXTRA_ID, id);
            data.putString(EXTRA_REMOTE_URL, urlString);
            data.putInt(EXTRA_ERROR, reason);
            sendBroadcast(data, ACTION_ERROR);
        }
    };
    
}
