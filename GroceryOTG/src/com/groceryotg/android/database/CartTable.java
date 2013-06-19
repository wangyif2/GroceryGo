package com.groceryotg.android.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * User: robert
 * Date: 23/02/13
 */
public class CartTable {
	// database table
	public static final String TABLE_CART = "cart";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_CART_GROCERY_ID = "cart_grocery_id"; // this is grocery_id
	public static final String COLUMN_CART_GROCERY_NAME = "cart_grocery_name";
	public static final String COLUMN_CART_FLAG_SHOPLIST = "cart_flag_shoplist";
	public static final String COLUMN_CART_FLAG_WATCHLIST = "cart_flag_watchlist";
	public static final int FLAG_TRUE = 1;
	public static final int FLAG_FALSE = 0;
	
	
	// database creation SQL statement
	public static final String DATABASE_CREATE = "create table "
			+ TABLE_CART
			+ "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_CART_GROCERY_ID + " integer unique, "
			+ COLUMN_CART_GROCERY_NAME + " text, "
			+ COLUMN_CART_FLAG_SHOPLIST + " integer, "
			+ COLUMN_CART_FLAG_WATCHLIST + " integer);";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(CategoryTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
		onCreate(database);
	}
}
