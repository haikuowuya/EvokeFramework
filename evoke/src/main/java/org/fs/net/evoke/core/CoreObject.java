package org.fs.net.evoke.core;

import android.util.Log;

import org.fs.net.evoke.util.LogUtil;

/**
 * Created by Fatih on 27/01/15.
 * as org.fs.net.evoke.core.CoreObject
 */
public abstract class CoreObject {
    
    protected void log(String message) {
        log(Log.DEBUG, message);
    }
    
    protected void log(int priority, String message) {
        if(isLogEnabled()) {
            String tag = getClassTag();
            if(null == tag) {
                LogUtil.log(priority, message);
            } else {
                LogUtil.log(priority, tag, message);
            }
        }
    }
    
    protected abstract boolean isLogEnabled();
    protected abstract String  getClassTag();
}
