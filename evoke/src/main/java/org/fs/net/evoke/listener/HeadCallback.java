package org.fs.net.evoke.listener;

import java.io.File;

/**
 * Created by Fatih on 30/01/15.
 * as org.fs.net.evoke.listener.HeadCallback
 */
public class HeadCallback {

    /**
     * Progress of the request download 
     * @param id
     * @param soFarDownloaded
     * @param total
     */
    public void onProgress(int id, long soFarDownloaded, long total) { }


    /**
     * When file downloaded we get this callback.
     * @param id
     * @param file
     */
    public void onComplete(int id, File file) { }


    /**
     * 
     * @param id
     * @param url
     * @param reason
     */
    public void onError(int id, String url, int reason) { }
}
