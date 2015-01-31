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

    
    private String urlString = "http://cdn.liverail.com/adasset4/20213/78215/251804/hi.mp4";//"http://onurodulkart.com/OnurMarketPanel/Pdf/635574243161639286_2201_04022015_insert.pdf";
       
    ImageView imageView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageView = new ImageView(this);
        setContentView(imageView);

        DownloadManager downloadManager = DownloadManager.getInstance(this);
        //downloadManager.start(Uri.parse(urlString));
        downloadManager.start(Uri.parse("http://cdn.liverail.com/adasset4/20213/74193/250304/lo.mp4"));
        downloadManager.start(Uri.parse("http://cdn.liverail.com/adasset4/20213/74193/250304/lo.webm"));
        downloadManager.start(Uri.parse("http://cdn.liverail.com/adasset4/20213/74193/250304/lo.flv"));
        downloadManager.start(Uri.parse("http://cdn.liverail.com/adasset4/20213/74193/250304/lo.wmv"));
        downloadManager.start(Uri.parse("http://cdn.liverail.com/adasset4/20213/74193/250304/me.mp4"));
        downloadManager.start(Uri.parse("http://cdn.liverail.com/adasset4/20213/74193/250304/me.webm"));
        downloadManager.start(Uri.parse("http://cdn.liverail.com/adasset4/20213/74193/250304/me.flv"));
        downloadManager.start(Uri.parse("http://cdn.liverail.com/adasset4/20213/74193/250304/me.wmv"));
        downloadManager.start(Uri.parse("http://cdn.liverail.com/adasset4/20213/74193/250304/hi.mp4"));
        downloadManager.start(Uri.parse("http://cdn.liverail.com/adasset4/20213/74193/250304/hi.webm"));
        downloadManager.start(Uri.parse("http://cdn.liverail.com/adasset4/20213/74193/250304/hi.flv"));
        downloadManager.start(Uri.parse("http://cdn.liverail.com/adasset4/20213/74193/250304/hi.wmv"));
    }
}
