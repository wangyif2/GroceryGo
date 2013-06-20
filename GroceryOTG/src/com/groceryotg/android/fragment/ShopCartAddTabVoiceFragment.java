package com.groceryotg.android.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

public class ShopCartAddTabVoiceFragment extends SherlockFragment {
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1000;
	
	private Context mContext;
	
	private TextView mText;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mContext = activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.shopcart_add_tab_voice, container, false);
		
		mText = (TextView) v.findViewById(R.id.voice_text);

		ImageButton micButton = (ImageButton) v.findViewById(R.id.voice_button);
		micButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
				intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.search_voice_prompt));
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
				intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
				startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
			}
		});
		
		Button confirmButton = (Button) v.findViewById(R.id.positive_button);
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addItem();
			}
		});
		Button clearButton = (Button) v.findViewById(R.id.negative_button);
		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clearFocus();
			}
		});
		
		return v;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
			
			ArrayList<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			 
			if (!textMatchList.isEmpty()) {
				String text = textMatchList.get(0);
				TextView textView = (TextView) ((Activity) mContext).findViewById(R.id.voice_text);
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
		
	private void makeToast(String text) {
		Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
	}
	
	private void clearFocus() {
		// clear the next in the edit box
		mText.setText("");
	}
	
	private void addItem() {
		String name = mText.getText().toString();
		
		clearFocus();
		
		if (TextUtils.isEmpty(name)) {
			makeToast("Please enter a name");
			return;
		}
		
		makeToast(getString(R.string.cart_shoplist_added));

		ContentValues values = new ContentValues();
		values.put(CartTable.COLUMN_CART_GROCERY_NAME, name);
		values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, CartTable.FLAG_TRUE);
		values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, CartTable.FLAG_FALSE);

		mContext.getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);
	}
}
