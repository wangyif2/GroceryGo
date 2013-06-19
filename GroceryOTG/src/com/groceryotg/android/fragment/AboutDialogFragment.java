package com.groceryotg.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.groceryotg.android.R;

public class AboutDialogFragment extends SherlockDialogFragment {
	Context mContext;
	
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
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String titleString = getString(R.string.about_title) + " v" + getString(R.string.version_name);
		
		Dialog dialog = new AlertDialog.Builder(getActivity())
			.setTitle(titleString)
			.setPositiveButton("Submit feedback", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("plain/text");
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"grocery.otg+support@gmail.com"});
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GroceryOTG feedback");
					mContext.startActivity(Intent.createChooser(emailIntent, "Send feedback"));
					
					dialog.dismiss();
				}
			})
			.setNegativeButton("Close", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			})
			.setView(((Activity) mContext).getLayoutInflater().inflate(R.layout.about_dialog, null))
			.create();
		
		dialog.setCanceledOnTouchOutside(false);
		
		return dialog;
	}
}
