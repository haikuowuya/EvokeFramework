package org.fs.net.evoke.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Fatih on 30/01/15.
 * as org.fs.net.evoke.database.DatabaseHelper
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    
    private DatabaseHelper(Context context) {
        //context, Name, factory = null, version
        super(context, "", null, 1);
        
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        //create tables.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //most cases drops tables but what about data been there ?
        onCreate(db);
    }
}
