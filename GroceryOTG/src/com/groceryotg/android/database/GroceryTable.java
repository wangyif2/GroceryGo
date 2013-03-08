package com.groceryotg.android.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * User: robert
 * Date: 07/02/13
 */
public class GroceryTable {

    // database table
    public static final String TABLE_GROCERY = "grocery";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_GROCERY_ID = "grocery_id";
    public static final String COLUMN_GROCERY_NAME = "grocery_name";
    public static final String COLUMN_GROCERY_PRICE = "grocery_price";
    public static final String COLUMN_GROCERY_CATEGORY = "grocery_category";
    public static final String COLUMN_GROCERY_EXPIRY = "grocery_expiry";
    public static final String COLUMN_GROCERY_STORE = "store_id";
    
    // database creation SQL statement
    public static final String DATABASE_CREATE = "create table "
            + TABLE_GROCERY
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_GROCERY_ID + " integer unique not null, "
            + COLUMN_GROCERY_NAME + " text not null, "
            + COLUMN_GROCERY_PRICE + " real, "
            + COLUMN_GROCERY_CATEGORY + " integer, "
            + COLUMN_GROCERY_EXPIRY + " integer, " 
            + COLUMN_GROCERY_STORE + " integer);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(CategoryTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_GROCERY);
        onCreate(database);
    }
}
