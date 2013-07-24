package ca.grocerygo.android;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;

import ca.grocerygo.android.utils.GroceryOTGUtils;
import ca.actionbarsherlock.app.SherlockFragmentActivity;
import ca.actionbarsherlock.view.Menu;
import ca.actionbarsherlock.view.MenuInflater;
import ca.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MapFragmentActivity extends SherlockFragmentActivity {
	public static final int CAM_ZOOM = 13;
	public static final String MAP_FRAGMENT_TAG = "map_fragment_tag";
	public static final String EXTRA_FILTER_STORE_PARENT = "extra_filter_store_parent";
	public static final String EXTRA_FILTER_STORE = "extra_filter_store";
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		
		GroceryOTGUtils.NavigationDrawerBundle drawerBundle = GroceryOTGUtils.configNavigationDrawer(this, false, R.string.title_map);
		this.mDrawerLayout = drawerBundle.getDrawerLayout();
		this.mDrawerList = drawerBundle.getDrawerList();
		
		boolean isGooglePlaySuccess = checkGooglePlayService();
		
		if (!isGooglePlaySuccess)
			finish();
	}
	
	private boolean checkGooglePlayService() {
		int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getApplicationContext());
		if (errorCode != ConnectionResult.SUCCESS) {
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, -1);
			if (errorDialog != null) {
				errorDialog.show();
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.map_activity_menu, menu);

		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (this.mDrawerLayout != null && this.mDrawerList != null) {
			for (int i = 0; i < menu.size(); i++) {
				MenuItem item = menu.getItem(i);
				item.setVisible(!mDrawerLayout.isDrawerOpen(mDrawerList));
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (mDrawerLayout.isDrawerOpen(mDrawerList))
					mDrawerLayout.closeDrawer(mDrawerList);
				else {
					Intent parentActivityIntent = new Intent(this, CategoryTopFragmentActivity.class);
					parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
							Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(parentActivityIntent);
					finish();
				}
				
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
