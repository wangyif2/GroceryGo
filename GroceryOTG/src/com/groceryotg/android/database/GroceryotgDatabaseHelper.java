package com.groceryotg.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * User: robert
 * Date: 07/02/13
 */
public class GroceryotgDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "groceryotg.db";
    private static final int DATABASE_VERSION = 1;

    public GroceryotgDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        CategoryTable.onCreate(db);
        GroceryTable.onCreate(db);
        StoreParentTable.onCreate(db);
        FlyerTable.onCreate(db);
        StoreTable.onCreate(db);
        CartTable.onCreate(db);
        WatchlistTable.onCreate(db);
        WatchlistItemTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CategoryTable.onUpgrade(db, oldVersion, newVersion);
        GroceryTable.onUpgrade(db, oldVersion, newVersion);
        StoreParentTable.onUpgrade(db, oldVersion, newVersion);
        FlyerTable.onUpgrade(db, oldVersion, newVersion);
        StoreTable.onUpgrade(db, oldVersion, newVersion);
    }

    public SQLiteDatabase getDb() {
        return this.getWritableDatabase();
    }
}
