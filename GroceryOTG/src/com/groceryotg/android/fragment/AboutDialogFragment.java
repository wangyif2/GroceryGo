package com.groceryotg.android.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.groceryotg.android.R;

public class AboutDialogFragment extends SherlockDialogFragment {
	public AboutDialogFragment() {
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.about, container, false);
		
		Dialog d = getDialog();
		d.setTitle(R.string.about_title);
		
		return view;
	}
}
