package ca.grocerygo.android.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

public class ParcelableStateVar implements Parcelable {
	private SparseArray<Float> mStoreDistanceMap;
    private Map<String, Integer> mMapIconMap = new HashMap<String, Integer>();
    private Map<String, Integer> mStoreParentIconMap = new HashMap<String, Integer>();
    private SparseArray<ArrayList<Integer>> mFlyerStoreMap = new SparseArray<ArrayList<Integer>>();
    private SparseArray<ArrayList<Integer>> mStoreParentStoreMap = new SparseArray<ArrayList<Integer>>();
    private Integer mGlobalSize;
    
    private static final String STATEVAR_STOREDISTANCEMAP = "store_distance_map";
    private static final String STATEVAR_MAPICONMAP = "map_icon_map";
    private static final String STATEVAR_STOREPARENTICONMAP = "storeparent_icon_map";
    private static final String STATEVAR_FLYERSTOREMAP = "flyer_store_map";
    private static final String STATEVAR_STOREPARENTSTOREMAP = "storeparent_store_map";
	
	// Constructor
	public ParcelableStateVar(SparseArray<Float> newStoreDistanceMap, Map<String, Integer> newMapIconMap, Map<String, Integer> newStoreParentIconMap, 
							  SparseArray<ArrayList<Integer>> newFlyerStoreMap, SparseArray<ArrayList<Integer>> newStoreParentStoreMap) {
		this.mStoreDistanceMap = newStoreDistanceMap;
		this.mMapIconMap = newMapIconMap;
		this.mStoreParentIconMap = newStoreParentIconMap;
		this.mFlyerStoreMap = newFlyerStoreMap;
		this.mStoreParentStoreMap = newStoreParentStoreMap;
		
		this.mGlobalSize = newStoreDistanceMap.size() + newMapIconMap.size() + newStoreParentIconMap.size() + newFlyerStoreMap.size() + newStoreParentStoreMap.size();
	}
	
	public void applyParcelableStateVar() {
		GroceryStoreDistanceMap.setmStoreDistanceMap(this.mStoreDistanceMap);
		GroceryStoreDistanceMap.setmMapIconMap(this.mMapIconMap);
		GroceryStoreDistanceMap.setmStoreParentIconMap(this.mStoreParentIconMap);
		GroceryStoreDistanceMap.setmFlyerStoreMap(this.mFlyerStoreMap);
		GroceryStoreDistanceMap.setmStoreParentStoreMap(this.mStoreParentStoreMap);
	}
	
	// Parcelling part
	public ParcelableStateVar(Parcel in) {
		// At this point none of the vars exist yet
		this.mStoreDistanceMap = new SparseArray<Float>();
		this.mMapIconMap = new HashMap<String, Integer>();
	    this.mStoreParentIconMap = new HashMap<String, Integer>();
	    this.mFlyerStoreMap = new SparseArray<ArrayList<Integer>>();
	    this.mStoreParentStoreMap = new SparseArray<ArrayList<Integer>>();
	    
		String[] parcelStringArray = in.createStringArray();
		this.mGlobalSize = parcelStringArray.length;
		for (int i=0; i < parcelStringArray.length; i++) {
			// convert "flyerID#storeID,storeID" into an entry into the sparseArray: <int, ArrayList<int>>
			String[] parts = parcelStringArray[i].split("#");
			String nextID = parts[0];
			
			if (nextID == STATEVAR_STOREDISTANCEMAP) {
				int nextKey = Integer.parseInt(parts[1]);
				Float nextVal = Float.parseFloat(parts[2]);
				this.mStoreDistanceMap.append(nextKey, nextVal);
			} else if (nextID == STATEVAR_MAPICONMAP) {
				String nextKey = parts[1];
				Integer nextVal = Integer.parseInt(parts[2]);
				this.mMapIconMap.put(nextKey, nextVal);
			} else if (nextID == STATEVAR_STOREPARENTICONMAP) {
				String nextKey = parts[1];
				Integer nextVal = Integer.parseInt(parts[2]);
				this.mStoreParentIconMap.put(nextKey, nextVal);
			} else if (nextID == STATEVAR_FLYERSTOREMAP) {
				int nextKey = Integer.parseInt(parts[1]);
				ArrayList<Integer> nextVal = new ArrayList<Integer>();
				String[] valsString = parts[2].split(",");
				for (int j=0; j < valsString.length; j++) {
					nextVal.add(Integer.parseInt(valsString[j]));
				}
				this.mFlyerStoreMap.append(nextKey, nextVal);
			} else if (nextID == STATEVAR_STOREPARENTSTOREMAP) {
				int nextKey = Integer.parseInt(parts[1]);
				ArrayList<Integer> nextVal = new ArrayList<Integer>();
				String[] valsString = parts[2].split(",");
				for (int j=0; j < valsString.length; j++) {
					nextVal.add(Integer.parseInt(valsString[j]));
				}
				this.mStoreParentStoreMap.append(nextKey, nextVal);
			}
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String[] parcelStringArray = new String[this.mGlobalSize];
		
		// Write all global vars to this string array
		int globalIndex;
		globalIndex = 0;
		for (int i=0; i < this.mStoreDistanceMap.size(); i++) {
			Integer nextKey = (Integer)this.mStoreDistanceMap.keyAt(i);
			Float nextVal = this.mStoreDistanceMap.valueAt(i);
			parcelStringArray[globalIndex++] = STATEVAR_STOREDISTANCEMAP + "#" + nextKey.toString() + "#" + nextVal.toString();
		}
		for (Map.Entry<String, Integer> entry : this.mMapIconMap.entrySet()) {
			String nextKey = entry.getKey();
			Integer nextVal = entry.getValue();
			parcelStringArray[globalIndex++] = STATEVAR_MAPICONMAP + "#" + nextKey + "#" + nextVal.toString();
		}
		for (Map.Entry<String, Integer> entry : this.mStoreParentIconMap.entrySet()) {
			String nextKey = entry.getKey();
			Integer nextVal = entry.getValue();
			parcelStringArray[globalIndex++] = STATEVAR_STOREPARENTICONMAP + "#" + nextKey + "#" + nextVal.toString();
		}
		for (int i=0; i < this.mFlyerStoreMap.size(); i++) {
			Integer nextKey = (Integer)this.mFlyerStoreMap.keyAt(i);
			ArrayList<Integer> nextVals = this.mFlyerStoreMap.valueAt(i);
			StringBuilder sb = new StringBuilder(nextVals.size());
			String sep = ",";
			for (int j=0; j < nextVals.size(); j++) {
				if (j > 0) sb.append(sep);
				sb.append(nextVals.get(j).toString());
			}
			parcelStringArray[globalIndex++] = STATEVAR_FLYERSTOREMAP + "#" + nextKey.toString() + "#" + sb.toString();
		}
		for (int i=0; i < this.mStoreParentStoreMap.size(); i++) {
			Integer nextKey = (Integer)this.mStoreParentStoreMap.keyAt(i);
			ArrayList<Integer> nextVals = this.mStoreParentStoreMap.valueAt(i);
			StringBuilder sb = new StringBuilder(nextVals.size());
			String sep = ",";
			for (int j=0; j < nextVals.size(); j++) {
				if (j > 0) sb.append(sep);
				sb.append(nextVals.get(j).toString());
			}
			parcelStringArray[globalIndex++] = STATEVAR_STOREPARENTSTOREMAP + "#" + nextKey.toString() + "#" + sb.toString();
		}
		dest.writeStringArray(parcelStringArray);
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ParcelableStateVar createFromParcel(Parcel in) {
            return new ParcelableStateVar(in); 
        }

        public ParcelableStateVar[] newArray(int size) {
            return new ParcelableStateVar[size];
        }
    };
	
}
