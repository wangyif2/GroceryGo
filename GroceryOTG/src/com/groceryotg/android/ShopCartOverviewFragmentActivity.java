package com.groceryotg.android;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.groceryotg.android.fragment.ShopCartOverviewFragment;

public class ShopCartOverviewFragmentActivity extends SherlockFragmentActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ShopCartOverviewFragment frag = new ShopCartOverviewFragment();
        frag.setHasOptionsMenu(true);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();
    }
}

