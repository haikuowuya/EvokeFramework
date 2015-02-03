package org.fs.net;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import org.fs.net.evoke.DownloadManager;
import org.fs.net.evoke.data.RequestObject;

import java.io.File;
import java.net.URI;

/**
 * Created by Fatih on 28/01/15.
 * as org.fs.net.MainActivity
 */
public class MainActivity extends Activity {

    
    private String urlString = "";
       
    ImageView imageView;
    IntentFilter filter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageView = new ImageView(this);
        setContentView(imageView);

        filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_PROGRESS);
        filter.addAction(DownloadManager.ACTION_ERROR);
        filter.addAction(DownloadManager.ACTION_COMPLETE);
        
        DownloadManager downloadManager = DownloadManager.getInstance(this);

        RequestObject.Builder builder = new RequestObject.Builder()
                .urlString("http://bakerframework.com/down/hpub-example-for-baker-3.2.zip");

        downloadManager.start(builder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcast, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcast);
    }

    BroadcastReceiver broadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if(action.equalsIgnoreCase(DownloadManager.ACTION_COMPLETE)) {
                int id = intent.getIntExtra(DownloadManager.EXTRA_ID, -1);
                String file = intent.getStringExtra(DownloadManager.EXTRA_FILE_URL);
                Log.println(Log.ERROR, MainActivity.class.getSimpleName(), String.format("%d - %s - %d", id, file, new File(URI.create(file)).length()));
            }
            else if(action.equalsIgnoreCase(DownloadManager.ACTION_PROGRESS)) {
                int id = intent.getIntExtra(DownloadManager.EXTRA_ID, -1);
                long downloaded = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOADED_SO_FAR, 0);
                long total      = intent.getLongExtra(DownloadManager.EXTRA_TOTAL, 0);
                
                Log.println(Log.ERROR, MainActivity.class.getSimpleName(), String.format("%d - %d - %d", id, downloaded, total));
            }
        }
    };
}
