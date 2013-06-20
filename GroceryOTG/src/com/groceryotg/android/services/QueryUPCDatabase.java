package com.groceryotg.android.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class QueryUPCDatabase extends AsyncTask<String, Void, String> {
	private Context mContext;
	private TextView mTextView;
	
	public QueryUPCDatabase(Context context, TextView textView) {
		this.mContext = context;
		this.mTextView = textView;
	}
	
	@Override
	protected String doInBackground(String... params) {
		try {
			URL databaseURL = new URL("http://api.upcdatabase.org/xml/" + params[0] + "/" + params[1]);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document document = builder.parse(new InputSource(databaseURL.openStream()));
			document.getDocumentElement().normalize();
			
			NodeList nodeList = document.getElementsByTagName("output");
			
			Node node = nodeList.item(0);
			
			NodeList itemNameList = ((Element) node).getElementsByTagName("itemname");
			if (itemNameList.getLength() == 0)
				return null;
			
			String itemName = itemNameList.item(0).toString();
			
			// TODO: Only get the first item for now, maybe add them as a list later
			return itemName;
			
		} catch (Exception e) {
			Log.e("GroceryOTG", "Could not construct upc database url. Exception " + e);
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(String name) {
		if (name == null)
			makeToast("Could not find that item in the database");
		else
			mTextView.setText(name);
	}
	
	private void makeToast(String text) {
		Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
	}
}
