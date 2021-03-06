package ca.grocerygo.android.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import ca.grocerygo.android.R;

import java.util.ArrayList;

public class ShopCartSummaryArrayAdapter extends ArrayAdapter<ShopCartSummaryItem> {
	private final Context mContext;
	private final ArrayList<ShopCartSummaryItem> mValues;
	private final int rowLayout;
	
	public ShopCartSummaryArrayAdapter(Context context, int rowLayout, ArrayList<ShopCartSummaryItem> values) {
		super(context, rowLayout, values);
		this.mContext = context;
		this.mValues = values;
		this.rowLayout = rowLayout;
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(rowLayout, parent, false);
		
		TextView storeParentId = (TextView) rowView.findViewById(R.id.shopcart_summary_storeparent_id);
		TextView storeParentName = (TextView) rowView.findViewById(R.id.shopcart_summary_storeparent);
		TextView storeTotal = (TextView) rowView.findViewById(R.id.shopcart_summary_total);
		TextView storeNumItems = (TextView) rowView.findViewById(R.id.shopcart_summary_numitems);
		
		storeParentId.setText(mValues.get(position).getStoreParentId());
		storeParentName.setText(mValues.get(position).getStoreParentName());
		storeTotal.setText("$" + mValues.get(position).getStoreTotal());
		storeNumItems.setText(mValues.get(position).getStoreTotalItems());
		
		return rowView;
	}
}
