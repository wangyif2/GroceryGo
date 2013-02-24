package com.groceryotg.android.database.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.groceryotg.android.database.*;

/**
 * User: robert
 * Date: 07/02/13
 */
public class GroceryotgProvider extends ContentProvider {
    // database
    private GroceryotgDatabaseHelper database;

    // Used for the UriMacher
    private static final int CATEGORIES = 10;
    private static final int CATEGORY_ID = 20;
    private static final int GROCERIES = 30;
    private static final int GROCERY_ID = 40;
    private static final int STORES = 50;
    private static final int STORE_ID = 60;
    private static final int CART_ITEMS = 70;
    private static final int CART_ITEM_ID = 80;

    // Content URI
    private static final String AUTHORITY = "com.groceryotg.android.database.contentprovider";
    private static final String BASE_PATH_CAT = "categories";
    private static final String BASE_PATH_GRO = "groceries";
    private static final String BASE_PATH_STO = "stores";
    private static final String BASE_PATH_CART = "cart_items";

    public static final Uri CONTENT_URI_CAT = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_CAT);
    public static final Uri CONTENT_URI_GRO = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_GRO);
    public static final Uri CONTENT_URI_STO = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_STO);
    public static final Uri CONTENT_URI_CART_ITEM = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_CART);

    // MIME type for multiple rows
    public static final String CONTENT_TYPE_CAT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/categories";
    public static final String CONTENT_ITEM_TYPE_CAT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/category";
    public static final String CONTENT_TYPE_GRO = ContentResolver.CURSOR_DIR_BASE_TYPE + "/groceries";
    public static final String CONTENT_ITEM_TYPE_GRO = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/grocery";
    public static final String CONTENT_TYPE_STO = ContentResolver.CURSOR_DIR_BASE_TYPE + "/stores";
    public static final String CONTENT_ITEM_TYPE_STO = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/store";
    public static final String CONTENT_TYPE_CART_ITEM = ContentResolver.CURSOR_DIR_BASE_TYPE + "/cart_items";
    public static final String CONTENT_ITEM_TYPE_CART_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/cart_item";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_CAT, CATEGORIES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_CAT + "/#", CATEGORY_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_GRO, GROCERIES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_GRO + "/#", GROCERY_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_STO, STORES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_STO + "/#", STORE_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_CART, CART_ITEMS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_CART + "/#", CART_ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        database = new GroceryotgDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case CATEGORIES:
                queryBuilder.setTables(CategoryTable.TABLE_CATEGORY);
                break;
            case CATEGORY_ID:
                queryBuilder.setTables(CategoryTable.TABLE_CATEGORY);
                queryBuilder.appendWhere(CategoryTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            case GROCERIES:
                queryBuilder.setTables(GroceryTable.TABLE_GROCERY);
                break;
            case GROCERY_ID:
                queryBuilder.setTables(GroceryTable.TABLE_GROCERY);
                queryBuilder.appendWhere(GroceryTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            case CART_ITEMS:
                queryBuilder.setTables(CartTable.TABLE_CART);
                break;
            case CART_ITEM_ID:
                queryBuilder.setTables(CartTable.TABLE_CART);
                queryBuilder.appendWhere(CartTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        //get readable?
        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case CATEGORIES:
                id = sqlDB.insert(CategoryTable.TABLE_CATEGORY, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(BASE_PATH_CAT + "/" + id);
            case GROCERIES:
                id = sqlDB.insert(GroceryTable.TABLE_GROCERY, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(BASE_PATH_GRO + "/" + id);
            case STORES:
                id = sqlDB.insert(StoreTable.TABLE_STORE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(BASE_PATH_STO + "/" + id);
            case CART_ITEMS:
                id = sqlDB.insert(CartTable.TABLE_CART, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(BASE_PATH_CART + "/" + id);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
