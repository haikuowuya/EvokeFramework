package org.fs.net.evoke.data;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fatih on 01/02/15.
 * as org.fs.net.evoke.data.Download
 */
public final class Download implements Serializable, Cloneable {

    public final static String TABLE_DOWNLOAD       = "download";

    public final static String COLUMN_ID            = "id";
    public final static String COLUMN_NAME          = "name";
    public final static String COLUMN_URI           = "uri";
    public final static String COLUMN_SIZE          = "size";
    public final static String COLUMN_POSITION      = "position";
    public final static String COLUMN_STATUS        = "status";
    
    private long        id;
    private String      name;
    private String      uri;
    private long        size;
    private long        position;
    private int         status;

    public Download() { }

    public static String createSQL() {
        return "create table " + TABLE_DOWNLOAD + " (" +
                    COLUMN_ID + " integer primary key autoincrement," +
                    COLUMN_NAME + " text," +
                    COLUMN_URI + " text, " +
                    COLUMN_SIZE + " integer," +
                    COLUMN_POSITION + " integer," +
                    COLUMN_STATUS + " integer);";
    }
    
    public static String dropSQL() {
        return "drop table if exists " + TABLE_DOWNLOAD + ";";        
    }
    
    public static List<Download> fromCursor(Cursor cursor) {
        int size = cursor.getCount();
        List<Download> downloads = new ArrayList<>(size);
        while (cursor.moveToNext()) {
            int indexId         = cursor.getColumnIndex(COLUMN_ID);
            int indexName       = cursor.getColumnIndex(COLUMN_NAME);
            int indexUri        = cursor.getColumnIndex(COLUMN_URI);
            int indexSize       = cursor.getColumnIndex(COLUMN_SIZE);
            int indexPosition   = cursor.getColumnIndex(COLUMN_POSITION);
            int indexStatus     = cursor.getColumnIndex(COLUMN_STATUS);
            
            Download download = new Download();
            download.setId(cursor.getInt(indexId));
            download.setName(cursor.getString(indexName));
            download.setUri(cursor.getString(indexUri));
            download.setSize(cursor.getInt(indexSize));
            download.setPosition(cursor.getInt(indexPosition));
            download.setStatus(cursor.getInt(indexStatus));
            
            downloads.add(download);
        }
        cursor.close();
        return downloads;
    }
    
    public ContentValues toContentValue() {
        ContentValues value = new ContentValues();
        
        value.put(COLUMN_NAME, name);
        value.put(COLUMN_URI, uri);
        value.put(COLUMN_SIZE, size);
        value.put(COLUMN_POSITION, position);
        value.put(COLUMN_STATUS, status);
        return value;
    }

    @Override
    public Download clone() throws CloneNotSupportedException {
        return (Download)super.clone();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}