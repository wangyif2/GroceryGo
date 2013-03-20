package com.groceryotg.android.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WatchlistTable {
    // database table
    public static final String TABLE_WATCHLIST = "watchlist";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_WATCHLIST_NAME = "watchlist_name";
    public static final String COLUMN_WATCHLIST_COLOUR = "watchlist_colour";
    public static final String COLUMN_WATCHLIST_LASTUPDATED = "watchlist_lastupdated";

    // database creation SQL statement
    public static final String DATABASE_CREATE = "create table "
            + TABLE_WATCHLIST
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_WATCHLIST_NAME + " text not null, "
            + COLUMN_WATCHLIST_COLOUR + " text not null, " 
            + COLUMN_WATCHLIST_LASTUPDATED + " text);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(CategoryTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_WATCHLIST);
        onCreate(database);
    }
}
