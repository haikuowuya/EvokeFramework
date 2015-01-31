package org.fs.net.evoke.data;

import java.io.File;

/**
 * Created by Fatih on 31/01/15.
 * as org.fs.net.evoke.data.RequestObject
 */
public final class RequestObject {
    
    private final String urlString;
    private final File   moveTo;
    private final long   limit;
    
    private RequestObject(final String urlString, final File moveTo, final long limit) {
        this.urlString = urlString;
        this.moveTo = moveTo;
        this.limit = limit;
    }
    
    
    
    
    public static class Builder {
        String urlString;
        File   moveTo;
        long   limit;
        
        public Builder() {


        }
    }
}
