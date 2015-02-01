package org.fs.net;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import org.fs.net.evoke.DownloadManager;

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

    }
}
