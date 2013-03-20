package com.groceryotg.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import com.actionbarsherlock.app.SherlockFragment;
import com.groceryotg.android.GroceryFragmentActivity;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.WatchlistTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

/**
 * User: robert
 * Date: 16/03/13
 */
public class CategoryGridFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private GridView gridview;
    private GridView gridview_watchlist;
    private SimpleCursorAdapter adapter;
    private SimpleCursorAdapter adapter_watchlist;
    
    private final int INDEX_LOADER_CAT = 0;
    private final int INDEX_LOADER_WATCHLIST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.category_fragment_list, container, false);
        gridview = (GridView) v.findViewById(R.id.gridview);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                GroceryFragmentActivity.mPager.setCurrentItem(position + 1, true);
            }
        });
        gridview.setEmptyView(v.findViewById(R.id.empty_category_list));
        
        gridview_watchlist = (GridView) v.findViewById(R.id.grid_watchlists);
        gridview_watchlist.setEmptyView(v.findViewById(R.id.empty_watchlist));
        
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillData();
    }

    private void fillData() {
        String[] from = new String[]{CategoryTable.COLUMN_CATEGORY_NAME};
        int[] to = new int[]{R.id.category_row_label};

        getLoaderManager().initLoader(INDEX_LOADER_CAT, null, this);
        adapter = new CategoryGridCursorAdapter(getActivity(), R.layout.category_fragment_row, null, from, to);

        gridview.setAdapter(adapter);
        
        // Get watchlists
        getLoaderManager().initLoader(INDEX_LOADER_WATCHLIST, null, this);
        String[] watchlist_from = new String[]{WatchlistTable.COLUMN_WATCHLIST_NAME, WatchlistTable.COLUMN_WATCHLIST_COLOUR};
        int[] watchlist_to = new int[]{R.id.category_watchlist_row_label};
        adapter_watchlist = new CategoryGridCursorAdapter(getActivity(), R.layout.category_watchlist_row, null, 
        													watchlist_from, watchlist_to);
        gridview_watchlist.setAdapter(adapter_watchlist);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    	if (id == INDEX_LOADER_CAT) {
	        String[] projection = {CategoryTable.COLUMN_ID, CategoryTable.COLUMN_CATEGORY_NAME};
	        return new CursorLoader(getActivity(), GroceryotgProvider.CONTENT_URI_CAT, projection, null, null, null);
    	}
    	else {
    		String[] projection = {WatchlistTable.COLUMN_ID, 
    							   WatchlistTable.COLUMN_WATCHLIST_NAME, 
    							   WatchlistTable.COLUMN_WATCHLIST_COLOUR,
    							   WatchlistTable.COLUMN_WATCHLIST_LASTUPDATED};
	        return new CursorLoader(getActivity(), GroceryotgProvider.CONTENT_URI_WATCHLIST, projection, null, null, null);
    	}
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    	if (cursorLoader.getId() == INDEX_LOADER_CAT) {
    		adapter.swapCursor(cursor);
    	}
    	else {
    		adapter_watchlist.swapCursor(cursor);
    	}
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    	if (cursorLoader.getId() == INDEX_LOADER_CAT) {
    		adapter.swapCursor(null);
    	}
    	else {
    		adapter_watchlist.swapCursor(null);
    	}
    }
}
