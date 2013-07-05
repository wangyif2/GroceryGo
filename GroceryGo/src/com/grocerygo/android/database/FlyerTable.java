package com.grocerygo.android.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * User: robert
 * Date: 15/02/13
 */
public class FlyerTable {
	// database table
	public static final String TABLE_FLYER = "flyer";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_FLYER_ID = "flyer_id";
	public static final String COLUMN_FLYER_URL = "flyer_url";
	public static final String COLUMN_FLYER_STOREPARENT = "store_parent_id";

	// database creation SQL statement
	public static final String DATABASE_CREATE = "create table "
			+ TABLE_FLYER
			+ "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_FLYER_ID + " integer unique not null, "
			+ COLUMN_FLYER_URL + " text not null, "
			+ COLUMN_FLYER_STOREPARENT + " integer, foreign key(" + COLUMN_FLYER_STOREPARENT
			+ ") references " + StoreParentTable.TABLE_STORE_PARENT + "(" + StoreParentTable.COLUMN_STORE_PARENT_ID + "));";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(CategoryTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_FLYER);
		onCreate(database);
	}
}
