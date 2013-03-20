package com.groceryotg.android.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WatchlistItemTable {
    // database table
    public static final String TABLE_WATCHLISTITEM = "watchlist_item";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_WATCHLISTITEM_LIST_ID = "watchlist_item_list_id";
    public static final String COLUMN_WATCHLISTITEM_NAME = "watchlist_item_name";
    public static final String COLUMN_WATCHLISTITEM_GROCERY_ID = "watchlist_item_grocery_id";

    // database creation SQL statement
    public static final String DATABASE_CREATE = "create table "
            + TABLE_WATCHLISTITEM
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_WATCHLISTITEM_LIST_ID + " integer not null, "
            + COLUMN_WATCHLISTITEM_NAME + " text not null, " 
            + COLUMN_WATCHLISTITEM_GROCERY_ID + " integer unique);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(CategoryTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_WATCHLISTITEM);
        onCreate(database);
    }
}
