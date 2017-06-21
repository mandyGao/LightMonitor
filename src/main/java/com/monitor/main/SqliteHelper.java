package com.monitor.main;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by scenic on 2017/6/20.
 */

public class SqliteHelper extends SQLiteOpenHelper{


    static MyLog log = new MyLog("SqliteHelper");
    public SqliteHelper(Context context, String name, int version) {
        super(context, name, null, version, new DatabaseErrorHandler() {
            @Override
            public void onCorruption(SQLiteDatabase dbObj) {

            }
        });
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        log.debug("oncreate " + db);
        db.execSQL(
                "create table if not exists LampInfo " +
                "(id integer primary key autoincrement ," +
                "lampCode varchar(255),lampName varchar(255)," +
                "lampStatus integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        log.debug("onUpgrade "+ db + "  " + oldVersion + "  " + newVersion);

    }
}
