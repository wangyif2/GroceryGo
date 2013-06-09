package com.groceryotg.android;

import android.content.Intent;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.groceryotg.android.fragment.GlobalSearchFragment;

public class GlobalSearchFragmentActivity extends SherlockFragmentActivity {
	public static final String GLOBAL_SEARCH = "global_search";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalSearchFragment frag = new GlobalSearchFragment();
        frag.setHasOptionsMenu(true);
        
        // Get the initial intent and pass it to the fragment as an argument
        Intent intent = getIntent();
        frag.setArguments(intent.getExtras());
        
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();
    }
	
	/*@Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        refreshQuery();
    }*/
}
