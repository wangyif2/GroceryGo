package com.groceryotg.android.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * User: robert
 * Date: 15/02/13
 */
public class StoreParentTable {
	// database table
	public static final String TABLE_STORE_PARENT = "store_parent";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_STORE_PARENT_ID = "store_parent_id";
	public static final String COLUMN_STORE_PARENT_NAME = "store_parent_name";

	// database creation SQL statement
	public static final String DATABASE_CREATE = "create table "
			+ TABLE_STORE_PARENT
			+ "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_STORE_PARENT_ID + " integer unique not null, "
			+ COLUMN_STORE_PARENT_NAME + " text not null);";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(CategoryTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_STORE_PARENT);
		onCreate(database);
	}
}
