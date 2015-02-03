package org.fs.net.evoke.data;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fatih on 01/02/15.
 * as org.fs.net.evoke.data.Error
 */
public final class Error implements Serializable, Cloneable {

    public final static String TABLE_NAME           = "error";
    
    public final static String COLUMN_ID            = "id";
    public final static String COLUMN_DETAIL        = "detail";
    public final static String COLUMN_DOWNLOAD_ID   = "download_id";
    
    private long    id;
    private String  detail;
    private long    downloadId;
    
    public Error() {}
    
    public static List<Error> fromCursor(Cursor cursor) {
        int size = cursor.getCount();
        List<Error> errors = new ArrayList<>(size);
        
        while(cursor.moveToNext()) {
            int indexId         = cursor.getColumnIndex(COLUMN_ID);
            int indexDetail     = cursor.getColumnIndex(COLUMN_DETAIL);
            int indexDownloadId = cursor.getColumnIndex(COLUMN_DOWNLOAD_ID);
        
            Error error = new Error();
            error.setId(cursor.getInt(indexId));
            error.setDetail(cursor.getString(indexDetail));
            error.setDownloadId(cursor.getInt(indexDownloadId));
            
            errors.add(error);
        }
        cursor.close();
        return errors;
    }
    
    public static String createSQL() {
        return "create table " + TABLE_NAME + " (" +
                    COLUMN_ID + " integer primary key autoincrement," +
                    COLUMN_DETAIL + " text," +
                    COLUMN_DOWNLOAD_ID + " integer);";
    }
    
    public static String dropSQL() {
        return "drop table if exists " + TABLE_NAME + ";";
    }

    public ContentValues toContentValue() {
        ContentValues value = new ContentValues();
        value.put(COLUMN_DETAIL, detail);
        value.put(COLUMN_DOWNLOAD_ID, downloadId);
        return value;
    }

    @Override
    public Error clone() throws CloneNotSupportedException {
        return (Error)super.clone();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    } 
    
}
