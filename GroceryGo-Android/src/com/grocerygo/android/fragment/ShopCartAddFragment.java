package com.grocerygo.android.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.grocerygo.android.R;
import com.grocerygo.android.database.CartTable;
import com.grocerygo.android.database.contentprovider.GroceryotgProvider;
import com.grocerygo.android.utils.GroceryOTGUtils;

public class ShopCartAddFragment extends SherlockFragment {
	private Context mContext;
	
	private EditText mEditText;

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
		View v = inflater.inflate(R.layout.shopcart_add_fragment, container, false);
		
		final Button confirmButton = (Button) v.findViewById(R.id.positive_button);
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addItem();
			}
		});
		
		final Button clearButton = (Button) v.findViewById(R.id.negative_button);
		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clearFocus();
			}
		});
		
		// Disable both buttons by default
		confirmButton.setEnabled(false);
		clearButton.setEnabled(false);
		
		mEditText = (EditText) v.findViewById(R.id.cart_grocery_edit_name);
		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					addItem();
					return true;
				}
				return false;
			}
		});
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s == null || s.length() == 0) {
					confirmButton.setEnabled(false);
					clearButton.setEnabled(false);
				} else {
					confirmButton.setEnabled(true);
					clearButton.setEnabled(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		return v;
	}

	private void makeToast(String text) {
		Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
	}
	
	private void clearFocus() {
		// close the soft keyboard
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
		// clear the next in the edit box
		mEditText.setText("");
		mEditText.clearFocus();
	}
	
	private void addItem() {
		String name = mEditText.getText().toString();
		
		clearFocus();
		
		makeToast(getString(R.string.cart_shoplist_added));

		ContentValues values = new ContentValues();
		values.put(CartTable.COLUMN_CART_GROCERY_NAME, name);
		values.putNull(CartTable.COLUMN_CART_GROCERY_ID);
		values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, CartTable.FLAG_TRUE);
		values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, CartTable.FLAG_FALSE);

		mContext.getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);
		
		GroceryOTGUtils.restartGroceryLoaders(mContext);
	}
}
