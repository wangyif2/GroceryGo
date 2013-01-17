package com.groceryotg.database;

/**
 * User: robert
 * Date: 17/01/13
 */
public class GroceryTable {

    // Database table
    public static final String TABLE_GROCERY = "grocery";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_CATEGORY = "price";
    public static final String COLUMN_STORE = "store";

    // Database creation SQL statement
    public static final String DATABASE_CREATE = "create table "
            + TABLE_GROCERY
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_PRICE + " integer not null"
            + COLUMN_CATEGORY + " text not null,"
            + COLUMN_STORE + " text not null" + ");";
}

