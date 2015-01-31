package org.fs.net.evoke.data;

import java.io.File;

/**
 * Created by Fatih on 29/01/15.
 * as org.fs.net.evoke.data.PartObject
 */
public class PartObject {
    
    private final File  file;
    private final String urlString;
    private final String range;
    
    private PartObject(final File file, final String range, final String urlString) {
        this.urlString = urlString;
        this.range = range;
        this.file = file;
    }

    @Override
    public String toString() {
        return String.format("\nrange:\t%s", getRange());
    }

    public String getRange() {
        return range;
    }

    public File getFile() {
        return file;
    }

    public String getUrlString() {
        return urlString;
    }

    public static class Builder {
        File file;
        String urlString;
        String range;
        
        public Builder() { }
        
        public Builder file(File file) {
            this.file = file;
            return this;
        }
        
        public Builder urlString(String urlString) {
            this.urlString = urlString;
            return this;
        }
        
        public Builder range(String range) {
            this.range = range;
            return this;
        }
        
        public PartObject build() {
            return new PartObject(file, range, urlString);
        }
    }
}
