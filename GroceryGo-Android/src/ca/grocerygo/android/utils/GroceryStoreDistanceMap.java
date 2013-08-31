package ca.grocerygo.android.utils;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * User: robert
 * Date: 31/08/13
 */
public class GroceryStoreDistanceMap {

    private static SparseArray<Float> mStoreDistanceMap;
    private static Map<String, Integer> mMapIconMap = new HashMap<String, Integer>();
    private static Map<String, Integer> mStoreParentIconMap = new HashMap<String, Integer>();
    private static SparseArray<ArrayList<Integer>> mFlyerStoreMap = new SparseArray<ArrayList<Integer>>();
    private static SparseArray<ArrayList<Integer>> mStoreParentStoreMap = new SparseArray<ArrayList<Integer>>();

    public static SparseArray<Float> getmStoreDistanceMap() {
        return mStoreDistanceMap;
    }

    public static void setmStoreDistanceMap(SparseArray<Float> mStoreDistanceMap) {
        GroceryStoreDistanceMap.mStoreDistanceMap = mStoreDistanceMap;
    }

    public static Map<String, Integer> getmMapIconMap() {
        return mMapIconMap;
    }

    public static void setmMapIconMap(Map<String, Integer> mMapIconMap) {
        GroceryStoreDistanceMap.mMapIconMap = mMapIconMap;
    }

    public static Map<String, Integer> getmStoreParentIconMap() {
        return mStoreParentIconMap;
    }

    public static void setmStoreParentIconMap(Map<String, Integer> mStoreParentIconMap) {
        GroceryStoreDistanceMap.mStoreParentIconMap = mStoreParentIconMap;
    }

    public static SparseArray<ArrayList<Integer>> getmFlyerStoreMap() {
        return mFlyerStoreMap;
    }

    public static void setmFlyerStoreMap(SparseArray<ArrayList<Integer>> mFlyerStoreMap) {
        GroceryStoreDistanceMap.mFlyerStoreMap = mFlyerStoreMap;
    }

    public static SparseArray<ArrayList<Integer>> getmStoreParentStoreMap() {
        return mStoreParentStoreMap;
    }

    public static void setmStoreParentStoreMap(SparseArray<ArrayList<Integer>> mStoreParentStoreMap) {
        GroceryStoreDistanceMap.mStoreParentStoreMap = mStoreParentStoreMap;
    }
}
