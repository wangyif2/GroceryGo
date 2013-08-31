package ca.grocerygo.android;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.os.Looper;
import ca.grocerygo.android.database.StoreParentTable;
import ca.grocerygo.android.database.StoreTable;
import ca.grocerygo.android.utils.GroceryOTGUtils;
import ca.grocerygo.android.utils.GroceryStoreDistanceMap;

import java.util.ArrayList;
import java.util.Locale;

public class GroceryApplication extends Application {

    public static final String TAG = "GroceryGo";
	
	public void constructGlobals(final Context context) {
		ArrayList<Thread> threads = new ArrayList<Thread>();
		Thread t;
		
		// Calculates store distances
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				
                GroceryStoreDistanceMap.setmStoreDistanceMap(GroceryOTGUtils.buildDistanceMap(context));
			}
		});
		threads.add(t);
		t.start();
		
		// Calculates icons for stores on the map
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				
				Cursor parents = GroceryOTGUtils.getStoreParentNamesCursor(context);
				parents.moveToFirst();
				while (!parents.isAfterLast()) {
					String name = parents.getString(parents.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME));
					int markerImageID = context.getResources().getIdentifier("ic_mapmarker_" + name.toLowerCase(Locale.CANADA).replace(" ", ""), "drawable", context.getPackageName());
					if (markerImageID != 0) {
                        GroceryStoreDistanceMap.getmMapIconMap().put(name, markerImageID);
					}
					parents.moveToNext();
				}
			}
		});
		threads.add(t);
		t.start();
		
		// Calculates icons for stores in the grocery list
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				
				Cursor parents = GroceryOTGUtils.getStoreParentNamesCursor(context);
				parents.moveToFirst();
				while (!parents.isAfterLast()) {
					String name = parents.getString(parents.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME));
					int storeIconID = context.getResources().getIdentifier("ic_store_" + name.toLowerCase(Locale.CANADA).replace(" ", ""), "drawable", context.getPackageName());
					if (storeIconID != 0) {
                        GroceryStoreDistanceMap.getmStoreParentIconMap().put(name, storeIconID);
					}
					parents.moveToNext();
				}
			}
		});
		threads.add(t);
		t.start();
		
		// Calculates mapping between flyer and stores, and 
		// mapping between storeParents and stores
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				
				Cursor storeIDs = GroceryOTGUtils.getStoreIDs(context);
				
				storeIDs.moveToFirst();
				while (!storeIDs.isAfterLast()) {
					int flyerId = storeIDs.getInt(storeIDs.getColumnIndex(StoreTable.COLUMN_STORE_FLYER));
					int storeId = storeIDs.getInt(storeIDs.getColumnIndex(StoreTable.COLUMN_STORE_ID));
					int storeParentId = storeIDs.getInt(storeIDs.getColumnIndex(StoreTable.COLUMN_STORE_PARENT));
					
					// Be careful: some stores have NULL flyers which gets translated to flyerId=0 here
					if (flyerId > 0) {
						if (GroceryStoreDistanceMap.getmFlyerStoreMap().get(flyerId) == null)
                            GroceryStoreDistanceMap.getmFlyerStoreMap().put(flyerId, new ArrayList<Integer>());
						ArrayList<Integer> n = GroceryStoreDistanceMap.getmFlyerStoreMap().get(flyerId);
						n.add(storeId);
                        GroceryStoreDistanceMap.getmFlyerStoreMap().put(flyerId, n);
						
						if (GroceryStoreDistanceMap.getmStoreParentStoreMap().get(storeParentId) == null)
							GroceryStoreDistanceMap.getmStoreParentStoreMap().put(storeParentId, new ArrayList<Integer>());
						// Now append the new value onto the end of the appropriate list
						ArrayList<Integer> m = GroceryStoreDistanceMap.getmStoreParentStoreMap().get(storeParentId);
						m.add(storeId);
						GroceryStoreDistanceMap.getmStoreParentStoreMap().put(storeParentId, m);
					}
					
					storeIDs.moveToNext();
				}
				
			}
		});
		threads.add(t);
		t.start();
		
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
