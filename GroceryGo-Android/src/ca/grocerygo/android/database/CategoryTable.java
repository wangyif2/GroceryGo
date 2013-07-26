package ca.grocerygo.android.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * User: robert
 * Date: 07/02/13
 */
public class CategoryTable {
	// database table
	public static final String TABLE_CATEGORY = "category";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_CATEGORY_ID = "category_id";
	public static final String COLUMN_CATEGORY_NAME = "category_name";

	// database creation SQL statement
	public static final String DATABASE_CREATE = "create table "
			+ TABLE_CATEGORY
			+ "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_CATEGORY_ID + " integer unique not null, "
			+ COLUMN_CATEGORY_NAME + " text not null" + ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(CategoryTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
		onCreate(database);
	}
}
