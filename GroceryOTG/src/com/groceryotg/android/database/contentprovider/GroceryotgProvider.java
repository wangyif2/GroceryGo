package com.groceryotg.android.database.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.GroceryotgDatabaseHelper;

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

    // Content URI
    private static final String AUTHORITY = "com.groceryotg.android.database.contentprovider";
    private static final String BASE_PATH = "categories";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    // MIME type for multiple rows
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/categories";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/category";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, CATEGORIES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CATEGORY_ID);
    }

    @Override
    public boolean onCreate() {
        database = new GroceryotgDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(CategoryTable.TABLE_CATEGORY);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case CATEGORIES:
                break;
            case CATEGORY_ID:
                queryBuilder.appendWhere(CategoryTable.COLUMN_ID + "=" + uri.getLastPathSegment());
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
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
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
