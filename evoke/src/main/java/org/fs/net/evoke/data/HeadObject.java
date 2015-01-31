package org.fs.net.evoke.data;

/**
 * Created by Fatih on 29/01/15.
 * as org.fs.net.evoke.th.HeadObject
 */

import java.util.UUID;

/**
 * HEAD OBJECT that been created in the result of request 
 */

public final class HeadObject {

    private final long length;
    private final String contentType;
    private final String name;
    private final String etag;
    /**
     * Remote file, in destination. 
     */
    private final String remote;

    private HeadObject(final String name, final String etag, final String contentType, final long length, final String remote) {
        this.length = length;
        this.contentType = contentType;
        this.name = name;
        this.etag = etag;
        this.remote = remote;
    }

    public long getLength() {
        return length;
    }

    public String getContentType() {
        return contentType;
    }

    public String getName() {
        return name;
    }

    public String getEtag() {
        return etag;
    }

    public String getRemote() {
        return remote;
    }

    @Override
    public String toString() {
        return String.format("\nname:\t\t%s" +
                             "\ncontent-type:\t%s" +
                             "\ne-tag:\t\t%s" +
                             "\nremote:\t\t%s" +
                             "\nlength:\t\t%d", getName(), getContentType(), getEtag(), getRemote(), getLength());
    }

    /**
     * Builder. 
     */
    public static class Builder {
        long length;
        String name;
        String etag;
        String remote;
        String contentType;

        public Builder() {
            this.etag = UUID.randomUUID().toString();
        }

        public Builder length(long length) {
            this.length = length;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder remote(String remote) {
            this.remote = remote;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public HeadObject build() {
            return new HeadObject(name, etag, contentType, length, remote);
        }
    }
}