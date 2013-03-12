package com.groceryotg.android.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * User: robert
 * Date: 15/02/13
 */
public class StoreTable {
    // database table
    public static final String TABLE_STORE = "store";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_STORE_ID = "store_id";
    public static final String COLUMN_STORE_ADDR = "store_addr";
    public static final String COLUMN_STORE_LATITUDE = "store_latitude";
    public static final String COLUMN_STORE_LONGITUDE = "store_longitude";
    public static final String COLUMN_STORE_PARENT = "store_parent_id";
    public static final String COLUMN_STORE_FLYER = "store_flyer_id";
    
    // database creation SQL statement
    public static final String DATABASE_CREATE = "create table "
            + TABLE_STORE
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_STORE_ID + " integer unique not null, "
            + COLUMN_STORE_ADDR + " text, "
            + COLUMN_STORE_LATITUDE + " real, "
            + COLUMN_STORE_LONGITUDE + " real, "
            + COLUMN_STORE_PARENT + " integer not null, "
            + COLUMN_STORE_FLYER + " integer not null);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(CategoryTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_STORE);
        onCreate(database);
    }
}
