package com.groceryotg.android.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

public class ShopCartAddTabCodeFragment extends SherlockFragment {
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
		View v = inflater.inflate(R.layout.shopcart_add_tab_code, container, false);
		
		mText = (TextView) v.findViewById(R.id.code_text);

		ImageButton micButton = (ImageButton) v.findViewById(R.id.barcode_button);
		micButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegrator integrator = new IntentIntegrator((Activity) mContext);
				integrator.initiateScan();
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
