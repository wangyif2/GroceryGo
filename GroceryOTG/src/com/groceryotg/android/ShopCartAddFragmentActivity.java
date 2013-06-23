package com.groceryotg.android;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.groceryotg.android.services.QueryUPCDatabase;
import com.groceryotg.android.utils.GroceryOTGUtils;

public class ShopCartAddFragmentActivity extends SherlockFragmentActivity {
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1000;
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	
	private final String SAVE_INSTANCE_CODE_TEXT = "save_instance_code_text";
	
	private String mCodeText;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.shopcart_add_activity);
		
		GroceryOTGUtils.NavigationDrawerBundle drawerBundle = GroceryOTGUtils.configNavigationDrawer(this, false, R.string.title_cart_detail);
		this.mDrawerLayout = drawerBundle.getDrawerLayout();
		this.mDrawerList = drawerBundle.getDrawerList();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(SAVE_INSTANCE_CODE_TEXT, mCodeText);
		
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    
	    mCodeText = savedInstanceState.getString(SAVE_INSTANCE_CODE_TEXT);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.shopcart_add_activity_menu, menu);
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
		case R.id.cart_add_by_voice:
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.cart_add_voice_prompt));
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
			break;
		case R.id.cart_add_by_barcode:
			IntentIntegrator integrator = new IntentIntegrator(this);
			integrator.initiateScan();
			break;
		case android.R.id.home:
			if (mDrawerLayout.isDrawerOpen(mDrawerList))
				mDrawerLayout.closeDrawer(mDrawerList);
			else {
				// Specify the parent activity
				Intent parentActivityIntent = new Intent(this, ShopCartOverviewFragmentActivity.class);
				parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
											Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(parentActivityIntent);
				this.finish();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// Need to add this here since the ZXing library creates a new activity from activity, not fragment
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (scanResult != null) {
			mCodeText = scanResult.getContents();
			setCode(mCodeText);
		}
		else if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
			
			ArrayList<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			 
			if (!textMatchList.isEmpty()) {
				String text = textMatchList.get(0);
				TextView textView = (TextView) findViewById(R.id.cart_grocery_edit_name);
				textView.setText(text);
			}
			} else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
				makeToast("Audio error");
			} else if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
				makeToast("Client error");
			} else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
				makeToast("Network error");
			} else if (resultCode == RecognizerIntent.RESULT_NO_MATCH){
				makeToast("Please repeat the item");
			} else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
				makeToast("Server error");
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void clearCodeText() {
		mCodeText = null;
	}
	
	private void makeToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	private void setCode(String code) {
		if (code == null)
			return;
		
		EditText nameView = (EditText) findViewById(R.id.cart_grocery_edit_name);
		String oldHint = (String) nameView.getHint();
		nameView.setHint("Searching for item " + code + " in the database...");
		
		QueryUPCDatabase q = new QueryUPCDatabase(this, nameView);
		q.execute(getString(R.string.upcdatabase_key), code);
		
		nameView.setHint(oldHint);
	}
}

