package org.fs.net.evoke.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.fs.net.evoke.data.Error;
import org.fs.net.evoke.data.Download;

import java.util.List;

/**
 * Created by Fatih on 30/01/15.
 * as org.fs.net.evoke.database.DatabaseHelper
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    
    private final static String DATABASE_NAME       = "evoke";
    private final static int    DATABASE_VERSION    = 1;    
    
    private static DatabaseHelper instance          = null;   
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public static DatabaseHelper getInstance(Context context) {
        if(instance == null) {
            if(context == null) {
                throw new IllegalArgumentException("Context object instance is null");
            }
            instance = new DatabaseHelper(context);
        }
        return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Download.createSQL());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(Download.dropSQL());
        onCreate(db);
    }
    
    public long addDownload(Download download) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(Download.TABLE_DOWNLOAD, null, download.toContentValue());
    }
    
    public List<Download> getDownloads() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Download.TABLE_DOWNLOAD, null, null, null, null, null, null);
        return Download.fromCursor(cursor);
    }
    
    public long addError(Error error) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(Error.TABLE_NAME, null, error.toContentValue());
    }
    
    public List<Error> getErrors(int downloadId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Error.TABLE_NAME, null, Error.COLUMN_DOWNLOAD_ID + "=?", new String[] { String.valueOf(downloadId) }, null, null, null);
        return Error.fromCursor(cursor);
    }
}
