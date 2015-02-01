# Evoke Framework
as you know downloading large files in android is mess so you need to manage connection and parts if you want to download with yourself
or you prefer android's build in DownloadManager but its damn so slow and uses default apache client to do network operation
this tools provides same functionality but it has used concurrent thread models and OkHttp to handle network for multi-core device performance influence.
 
```
   DownloadManager downloadManager = DownloadManager.getInstance(getApplicationContext());
   
   RequestObject.Builder builder = new RequestObject.Builder()
                               .urlString("REMOTE_FILE_URL") //only http or https supported.
                               .moveTo(new File(getFilesDir(), "abc.x")) //after download file will be move to this location as you wanted.
                               .length(-1); //if you set value x > 0 here it will be size we want to download if not defined we do full download.
   
   //add request into there
   downloadManager.start(builder.build());
```
   to receive callbacks for download status or completes progress etc.
   
```
   @Override onResume() {
        super.onResume();
        IntentFilter filters = new IntentFilter();
        filters.addAction(DownloadManager.ACTION_PROGRESS);
        filters.addAction(DownloadManager.ACTION_ERROR);
        filters.addAction(DownloadManager.ACTION_COMPLETE);
        
        registerReciever(broadcast, filters);
   }
   
   @Override onPause() {
        super.onPause();
        unregisterReceiver(broadcast);
   }
   
   BroadcastReceiver broadcast = new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {
           //you get data here
       }
   };
```