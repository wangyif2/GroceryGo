package com.groceryotg.android.database.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
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
    private static final int GROCERIES_JOINSTORE = 90;
    private static final int GROCERIES_JOINSTORE_ID = 100;
    private static final int STORE_PARENTS = 110;
    private static final int STORE_PARENT_ID = 120;
    private static final int FLYERS = 130;
    private static final int FLYER_ID = 140;
    private static final int STORE_JOIN_STOREPARENT = 150;

    // Content URI
    private static final String AUTHORITY = "com.groceryotg.android.database.contentprovider";
    private static final String BASE_PATH_CAT = "categories";
    private static final String BASE_PATH_GRO = "groceries";
    private static final String BASE_PATH_STO = "stores";
    private static final String BASE_PATH_STOPARENT = "storeparents";
    private static final String BASE_PATH_FLYER = "flyers";
    private static final String BASE_PATH_CART = "cart_items";

    // Joins
    private static final String BASE_PATH_GRO_JOINSTORE = "groceriesWithStore";
    private static final String BASE_PATH_STO_JOIN_STOREPARENT = "storeWithStoreParent";


    public static final Uri CONTENT_URI_CAT = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_CAT);
    public static final Uri CONTENT_URI_GRO = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_GRO);
    public static final Uri CONTENT_URI_STO = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_STO);
    public static final Uri CONTENT_URI_STOPARENT = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_STOPARENT);
    public static final Uri CONTENT_URI_CART_ITEM = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_CART);
    public static final Uri CONTENT_URI_FLYER = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_FLYER);
    // Join URIs
    public static final Uri CONTENT_URI_GRO_JOINSTORE = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_GRO_JOINSTORE);
    public static final Uri CONTENT_URI_STO_JOIN_STOREPARENT = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH_STO_JOIN_STOREPARENT);


    // MIME type for multiple rows
    public static final String CONTENT_TYPE_CAT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/categories";
    public static final String CONTENT_ITEM_TYPE_CAT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/category";
    public static final String CONTENT_TYPE_GRO = ContentResolver.CURSOR_DIR_BASE_TYPE + "/groceries";
    public static final String CONTENT_ITEM_TYPE_GRO = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/grocery";
    public static final String CONTENT_TYPE_STO = ContentResolver.CURSOR_DIR_BASE_TYPE + "/stores";
    public static final String CONTENT_ITEM_TYPE_STO = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/store";
    public static final String CONTENT_TYPE_STOPARENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/storeparents";
    public static final String CONTENT_ITEM_TYPE_STOPARENT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/storeparent";
    public static final String CONTENT_ITEM_FLYER = ContentResolver.CURSOR_DIR_BASE_TYPE + "/flyers";
    public static final String CONTENT_ITEM_TYPE_FLYER = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/flyer";
    public static final String CONTENT_TYPE_CART_ITEM = ContentResolver.CURSOR_DIR_BASE_TYPE + "/cart_items";
    public static final String CONTENT_ITEM_TYPE_CART_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/cart_item";
    // Joins
    public static final String CONTENT_TYPE_GRO_JOINSTORE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/groceriesWithStore";
    public static final String CONTENT_ITEM_TYPE_GRO_JOINSTORE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/groceryWithStore";
    public static final String CONTENT_TYPE_STO_JOIN_STOREPARENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/storeWithStoreParent";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_CAT, CATEGORIES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_CAT + "/#", CATEGORY_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_GRO, GROCERIES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_GRO + "/#", GROCERY_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_STO, STORES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_STO + "/#", STORE_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_STOPARENT, STORE_PARENTS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_STOPARENT + "/#", STORE_PARENT_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_FLYER, FLYERS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_FLYER + "/#", FLYER_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_CART, CART_ITEMS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_CART + "/#", CART_ITEM_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_GRO_JOINSTORE, GROCERIES_JOINSTORE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_GRO_JOINSTORE + "/#", GROCERIES_JOINSTORE_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_STO_JOIN_STOREPARENT, STORE_JOIN_STOREPARENT);
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
            case STORES:
                queryBuilder.setTables(StoreTable.TABLE_STORE);
                break;
            case STORE_PARENTS:
                queryBuilder.setTables(StoreParentTable.TABLE_STORE_PARENT);
                break;
            case FLYERS:
                queryBuilder.setTables(FlyerTable.TABLE_FLYER);
                break;
            case GROCERIES_JOINSTORE:
                // Grocery LEFT OUTER JOIN Flyer ON Flyer.flyer_id=Grocery.flyer_id
                //         LEFT OUTER JOIN StoreParent ON StoreParent.storeparent_id=Flyer.storeparent_id
                //         LEFT OUTER JOIN ShoppingCart ON ShoppingCart.grocery_id=Grocery.grocery_id
                queryBuilder.setTables(GroceryTable.TABLE_GROCERY + " LEFT OUTER JOIN " + FlyerTable.TABLE_FLYER
                        + " ON " + GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_FLYER
                        + "=" + FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_ID
                        + " LEFT OUTER JOIN " + StoreParentTable.TABLE_STORE_PARENT
                        + " ON " + FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_STOREPARENT
                        + "=" + StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID
                        + " LEFT OUTER JOIN " + CartTable.TABLE_CART + " ON "
                        + CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_ID + "="
                        + GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_ID);
                break;
            case GROCERIES_JOINSTORE_ID:
                queryBuilder.setTables(GroceryTable.TABLE_GROCERY + " LEFT OUTER JOIN " + FlyerTable.TABLE_FLYER
                        + " ON " + GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_FLYER
                        + "=" + FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_ID);
                queryBuilder.appendWhere(GroceryTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            case STORE_JOIN_STOREPARENT:
                queryBuilder.setTables(StoreTable.TABLE_STORE + " LEFT OUTER JOIN " + StoreParentTable.TABLE_STORE_PARENT
                        + " ON " + StoreTable.TABLE_STORE + "." + StoreTable.COLUMN_STORE_PARENT
                        + "=" + StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID);
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
            case STORE_PARENTS:
                id = sqlDB.insert(StoreParentTable.TABLE_STORE_PARENT, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(BASE_PATH_STOPARENT + "/" + id);
            case CART_ITEMS:
                id = sqlDB.insert(CartTable.TABLE_CART, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(BASE_PATH_CART + "/" + id);
            case GROCERIES_JOINSTORE:
                throw new IllegalArgumentException("Invalid URI, can't insert into join: " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case GROCERIES:
                rowsDeleted = sqlDB.delete(GroceryTable.TABLE_GROCERY, selection, selectionArgs);
                break;
            case CART_ITEMS:
                rowsDeleted = sqlDB.delete(CartTable.TABLE_CART, selection, selectionArgs);
                break;
            case CART_ITEM_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(CartTable.TABLE_CART, CartTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(CartTable.TABLE_CART, CartTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case GROCERIES_JOINSTORE_ID:
                throw new IllegalArgumentException("Invalid URI, can't delete from join: " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case CART_ITEMS:
                rowsUpdated = sqlDB.update(CartTable.TABLE_CART, values, selection, selectionArgs);
                break;
            case CART_ITEM_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(CartTable.TABLE_CART, values, CartTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDB.update(CartTable.TABLE_CART, values, CartTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case GROCERIES_JOINSTORE_ID:
                throw new IllegalArgumentException("Invalid URI, can't update a join: " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
