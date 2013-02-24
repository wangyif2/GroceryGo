package com.groceryotg.android;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

/**
 * User: robert
 * Date: 23/02/13
 */
public class ShopCartOverView extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>{


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
