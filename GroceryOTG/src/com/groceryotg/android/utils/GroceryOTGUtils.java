package com.groceryotg.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.groceryotg.android.CategoryTopFragmentActivity;
import com.groceryotg.android.GroceryPagerFragmentActivity;
import com.groceryotg.android.MapFragmentActivity;
import com.groceryotg.android.R;
import com.groceryotg.android.ShopCartOverviewFragmentActivity;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.fragment.AboutDialogFragment;
import com.groceryotg.android.settings.SettingsActivity;
import com.groceryotg.android.settings.SettingsManager;

import java.util.Set;

public class GroceryOTGUtils {

    public static Cursor getStoreLocations(Context context) {
        String[] projection = {StoreTable.TABLE_STORE+"."+StoreTable.COLUMN_STORE_ID,
        		StoreParentTable.TABLE_STORE_PARENT+"."+StoreParentTable.COLUMN_STORE_PARENT_ID,
        		StoreParentTable.TABLE_STORE_PARENT+"."+StoreParentTable.COLUMN_STORE_PARENT_NAME,
        		StoreTable.TABLE_STORE+"."+StoreTable.COLUMN_STORE_LATITUDE,
        		StoreTable.TABLE_STORE+"."+StoreTable.COLUMN_STORE_LONGITUDE};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_STO_JOIN_STOREPARENT, projection, null, null, null);
        return c;
    }
    
    public static Cursor getStoreFlyerIDs(Context context) {
    	String[] projection = {StoreTable.TABLE_STORE+"."+StoreTable.COLUMN_STORE_ID,
    			StoreTable.TABLE_STORE+"."+StoreTable.COLUMN_STORE_FLYER};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_STO, projection, null, null, null);
        return c;
    }

    public static Cursor getStoreParentNamesCursor(Context context) {
        String[] projection = {StoreParentTable.COLUMN_STORE_PARENT_ID, StoreParentTable.COLUMN_STORE_PARENT_NAME};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_STOPARENT, projection, null, null, null);
        return c;
    }

    public static Cursor getGroceriesFromCartFromStores(Context context) {
        String[] projection = {CartTable.COLUMN_CART_GROCERY_ID, CartTable.COLUMN_CART_GROCERY_NAME, StoreTable.COLUMN_STORE_ID, StoreTable.COLUMN_STORE_LATITUDE, StoreTable.COLUMN_STORE_LONGITUDE};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_CART_JOIN_STORE, projection, null, null, null);
        return c;
    }

    public static int getMaxGroceryId(Context context) {
        String[] projection = {"Max(" + GroceryTable.COLUMN_GROCERY_ID + ")"};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_GRO, projection, null, null, null);
        c.moveToFirst();
        return c.getInt(0);
    }
    
    public static SparseArray<String> getCategorySets(Context context) {
        SparseArray<String> categories = new SparseArray<String>();
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_CAT, null, null, null, null);

        c.moveToFirst();
        while (!c.isAfterLast()) {
            categories.put(
                    c.getInt(c.getColumnIndexOrThrow(CategoryTable.COLUMN_CATEGORY_ID)),
                    c.getString(c.getColumnIndexOrThrow(CategoryTable.COLUMN_CATEGORY_NAME)));
            c.moveToNext();
        }
        return categories;
    }

    public static SparseArray<String> getStoreParentNameSets(Context context) {
    	SparseArray<String> storeNames = new SparseArray<String>();

        Cursor storeCursor = GroceryOTGUtils.getStoreParentNamesCursor(context);
        if (storeCursor != null) {
            storeCursor.moveToFirst();
            while (!storeCursor.isAfterLast()) {
                storeNames.put(storeCursor.getInt(storeCursor.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_ID)),
                        storeCursor.getString(storeCursor.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME)));
                storeCursor.moveToNext();
            }
        }
        return storeNames;
    }

    /**
     * Method copies the intent extras from the received intent to the intent
     * that will be dispatched.
     *
     * @param aReceived
     * @param aDispatch
     */
    public static void copyIntentData(Intent aReceived, Intent aDispatch) {
        Set<String> lKeys = aReceived.getExtras().keySet();

        for (String lKey : lKeys) {
            aDispatch.putExtra(lKey, aReceived.getStringExtra(lKey));
        }
    }
    
    public static class NavigationDrawerBundle {
    	private DrawerLayout mDrawerLayout;
		private ListView mDrawerList;
    	private ActionBarDrawerToggle mDrawerToggle;
    	
    	public NavigationDrawerBundle(DrawerLayout drawerLayout, ListView drawerList, ActionBarDrawerToggle drawerToggle) {
    		this.mDrawerLayout = drawerLayout;
    		this.mDrawerList = drawerList;
    		this.mDrawerToggle = drawerToggle;
    	}
    	
    	/**
		 * @return the drawerLayout
		 */
		public DrawerLayout getDrawerLayout() {
			return mDrawerLayout;
		}

		/**
		 * @return the drawerList
		 */
		public ListView getDrawerList() {
			return mDrawerList;
		}

		/**
		 * @return the drawerToggle
		 */
		public ActionBarDrawerToggle getDrawerToggle() {
			return mDrawerToggle;
		}
    }
    
    public static void configActionBar(Activity activity) {
    	((SherlockFragmentActivity) activity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    	((SherlockFragmentActivity) activity).getSupportActionBar().setHomeButtonEnabled(true);
    }
    
    public static GroceryOTGUtils.NavigationDrawerBundle configNavigationDrawer(final Activity activity, boolean isTopView, final int titleResId) {
    	DrawerLayout drawerLayout;
		ListView drawerList;
    	ActionBarDrawerToggle drawerToggle = null;
    	
    	configActionBar(activity);
    	NavigationDrawerListViewModel[] rowModels = new NavigationDrawerListViewModel[] {
    			new NavigationDrawerListViewModel(R.string.navdrawer_heading_places, -1, true),
    			new NavigationDrawerListViewModel(R.string.navdrawer_item_cat, R.drawable.ic_menu_home, false),
    			new NavigationDrawerListViewModel(R.string.navdrawer_item_cart, R.drawable.ic_menu_cart, false),
    			new NavigationDrawerListViewModel(R.string.navdrawer_item_map, R.drawable.ic_menu_map, false),
    			new NavigationDrawerListViewModel(R.string.navdrawer_heading_tools, -1, true),
    			new NavigationDrawerListViewModel(R.string.navdrawer_item_settings, R.drawable.ic_menu_settings, false),
    			new NavigationDrawerListViewModel(R.string.navdrawer_item_about, R.drawable.ic_menu_about, false),
    			new NavigationDrawerListViewModel(R.string.navdrawer_item_changelog, R.drawable.ic_menu_about, false)
    	};
    	
    	drawerLayout = (DrawerLayout) activity.findViewById(R.id.navigation_drawer_layout);
    	drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
    		public void onDrawerClosed(View view) {
    			((SherlockFragmentActivity) activity).getSupportActionBar().setTitle(activity.getString(titleResId));
    		}
    		public void onDrawerOpened(View drawerView) {
    			if (!SettingsManager.getNavigationDrawerSeen(activity))
    				SettingsManager.setNavigationDrawerSeen(activity, true);
    			((SherlockFragmentActivity) activity).getSupportActionBar().setTitle(activity.getString(R.string.app_name));
    		}
    	});
    	
    	drawerList = (ListView) activity.findViewById(R.id.navigation_drawer_view);
    	drawerList.setAdapter(new GroceryOTGUtils.NavigationDrawerAdapter(activity, rowModels));
    	drawerList.setOnItemClickListener(new NavigationDrawerItemClickListener(activity, drawerLayout, drawerList));
    	
    	// Only set up toggling when at a top view
    	if (isTopView) {
	    	drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, R.drawable.ic_drawer, R.string.navdrawer_open, R.string.navdrawer_closed) {
	    		public void onDrawerClosed(View view) {
	    			((SherlockFragmentActivity) activity).getSupportActionBar().setTitle(activity.getString(titleResId));
	    		}
	    		public void onDrawerOpened(View drawerView) {
	    			if (!SettingsManager.getNavigationDrawerSeen(activity))
        				SettingsManager.setNavigationDrawerSeen(activity, true);
	    			((SherlockFragmentActivity) activity).getSupportActionBar().setTitle(activity.getString(R.string.app_name));
	    		}
	    	};
	    	drawerLayout.setDrawerListener(drawerToggle);
	    	
	    	// Handle first-time viewing of navigaton drawer
	    	if (!SettingsManager.getNavigationDrawerSeen(activity)) {
	    		drawerLayout.openDrawer(drawerList);
	    	}
    	}
    	
    	GroceryOTGUtils.NavigationDrawerBundle bundle = new GroceryOTGUtils.NavigationDrawerBundle(drawerLayout, drawerList, drawerToggle);
    	
    	return bundle;
    	
    }
    
    private static class NavigationDrawerListViewModel {
    	public int mTitleResId;
    	public int mIconResId;
    	public boolean mIsHeader;
    	
    	public NavigationDrawerListViewModel(int titleResId, int iconResId, boolean isHeader) {
    		this.mIconResId = iconResId;
    		this.mTitleResId = titleResId;
    		this.mIsHeader = isHeader;
    	}
    }
    
    private static class NavigationDrawerAdapter extends BaseAdapter {
    	Context mContext;
    	NavigationDrawerListViewModel[] mRowModels;
    	int mCount;
    	LayoutInflater mInflater;
    	
    	public NavigationDrawerAdapter(Context context, NavigationDrawerListViewModel[] rowModels) {
    		this.mContext = context;
    		this.mRowModels = rowModels;
    		
    		this.mCount = mRowModels.length;
    	}

		@Override
		public int getCount() {
			return this.mCount;
		}
		
		@Override
		public NavigationDrawerListViewModel getItem(int position) {
			return mRowModels[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public int getItemViewType(int position) {
			return getItem(position).mIsHeader ? 0 : 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView titleView;
			ImageView iconView;
			
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView;
			
			if (getItem(position).mIconResId > 0) {
				itemView = mInflater.inflate(R.layout.navdrawer_item, parent, false);
				iconView = (ImageView) itemView.findViewById(R.id.navdrawer_item_icon);
				iconView.setImageResource(getItem(position).mIconResId);
			} else {
				itemView = mInflater.inflate(R.layout.navdrawer_header, parent, false);
			}
			
			titleView = (TextView) itemView.findViewById(R.id.navdrawer_item_title);
			titleView.setText(getItem(position).mTitleResId);
			
			return itemView;
		}
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return !getItem(position).mIsHeader;
		}
    }
    
    private static class NavigationDrawerItemClickListener implements ListView.OnItemClickListener {
    	Context mContext;
    	private DrawerLayout mDrawerLayout;
    	private ListView mDrawerList;
    	
    	NavigationDrawerItemClickListener(Context context, DrawerLayout drawerLayout, ListView drawerList) {
    		this.mContext = context;
    		this.mDrawerLayout = drawerLayout;
    		this.mDrawerList = drawerList;
    	}
    	
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (mDrawerLayout.isDrawerOpen(mDrawerList))
        		mDrawerLayout.closeDrawer(mDrawerList);
			
			switch (position) {
			case 1:
				launchHomeActivity(mContext);
				break;
			case 2:
				launchShopCartActivity(mContext);
				break;
			case 3:
				launchMapActivity(mContext);
				break;
			case 5:
				launchSettingsActivity(mContext);
				break;
			case 6:
				launchAboutDialog(mContext);
				break;
			case 7:
				ChangeLogDialog cd = new ChangeLogDialog(mContext);
				cd.show();
				break;
			}
		}
    }
    
    public static void launchHomeActivity(Context context) {
        Intent intent = new Intent(context, CategoryTopFragmentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
    
    public static void launchGroceryPagerActivity(Context context, int position) {
        Intent intent = new Intent(context, GroceryPagerFragmentActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Add an extra to tell the pager to return to the first page
        Bundle extras = new Bundle();
        extras.putInt(GroceryPagerFragmentActivity.EXTRA_LAUNCH_PAGE, position);
        intent.putExtras(extras);
        
        context.startActivity(intent);
    }
    
    public static void launchMapActivity(Context context) {
        Intent intent = new Intent(context, MapFragmentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
    
    public static void launchShopCartActivity(Context context) {
        Intent intent = new Intent(context, ShopCartOverviewFragmentActivity.class);
        context.startActivity(intent);
    }
    
    public static void launchSettingsActivity(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }
    
    public static void launchAboutDialog(Context context) {
    	AboutDialogFragment dialog = new AboutDialogFragment();
    	dialog.show(((SherlockFragmentActivity) context).getSupportFragmentManager(), "about_dialog");
    }
}
