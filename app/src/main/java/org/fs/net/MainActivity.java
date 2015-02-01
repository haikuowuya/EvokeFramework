package org.fs.net;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;

import org.fs.net.evoke.DownloadManager;
import org.fs.net.evoke.data.Download;

/**
 * Created by Fatih on 28/01/15.
 * as org.fs.net.MainActivity
 */
public class MainActivity extends Activity {

    
    private String urlString = "";
       
    ImageView imageView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageView = new ImageView(this);
        setContentView(imageView);

        DownloadManager downloadManager = DownloadManager.getInstance(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_PROGRESS);
        filter.addAction(DownloadManager.ACTION_ERROR);
        filter.addAction(DownloadManager.ACTION_COMPLETE);
        
        registerReceiver(broadcast, filter);
        unregisterReceiver(broadcast);
        
    }
    
    BroadcastReceiver broadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            
        }
    };
}
