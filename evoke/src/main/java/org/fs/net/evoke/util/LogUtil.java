package org.fs.net.evoke.util;

import android.util.Log;

/**
 * Created by Fatih on 27/01/15.
 * as org.fs.net.evoke.util.LogUtil
 */
public class LogUtil {
    
    public static boolean DEBUG = true;
    
    public static void log(String message) {
        log(Log.DEBUG, message);
    }
    
    public static void log(int priority, String message) {
        log(priority, getClassTag(), message);
    }
    
    public static void log(int priority, String tag, String message) {
        if(isLogEnabled()) {
            Log.println(priority, tag, message);
        }
    }
    
    protected static boolean isLogEnabled() {
        return DEBUG;            
    }
    
    protected static String getClassTag() {
        return LogUtil.class.getSimpleName();            
    }
}
