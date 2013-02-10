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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CategoryTable.onUpgrade(db, oldVersion, newVersion);
        GroceryTable.onUpgrade(db, oldVersion, newVersion);
    }
}
