package com.groceryotg.android;

import java.util.ArrayList;

import com.groceryotg.android.utils.GroceryOTGUtils;

import android.app.Application;
import android.content.Context;
import android.util.SparseArray;

public class GroceryApplication extends Application {
	
	private SparseArray<Float> mDistanceMap;
	
	public void constructGlobals(final Context context) {
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		Thread t;
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				mDistanceMap = GroceryOTGUtils.buildDistanceMap(context);
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
