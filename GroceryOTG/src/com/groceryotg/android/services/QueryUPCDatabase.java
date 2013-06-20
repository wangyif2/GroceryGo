package com.groceryotg.android.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.StringWriter;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
			URL databaseURL = new URL("http://upcdatabase.org/api/xml/" + params[0] + "/" + params[1]);
			
			Log.i("GroceryOTG", "http://upcdatabase.org/api/xml/" + params[0] + "/" + params[1]);
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document document = builder.parse(new InputSource(databaseURL.openStream()));
			document.getDocumentElement().normalize();
			
			NodeList nodeList = document.getElementsByTagName("output");
			
			Node node = nodeList.item(0);
			
			NodeList itemNameList = ((Element) node).getElementsByTagName("itemname");
			if (itemNameList.getLength() == 0)
				return null;
			String itemName = nodeToString(itemNameList.item(0));
			
			NodeList descriptionList = ((Element) node).getElementsByTagName("description");
			if (descriptionList.getLength() > 0)
				itemName += nodeToString(descriptionList.item(0));
			
			// TODO: Only get the first item for now, maybe add them as a list later
			return itemName;
			
		} catch (Exception e) {
			e.printStackTrace();
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
	
	private String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.METHOD, "text");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			System.out.println("nodeToString Transformer Exception");
		}
		return sw.toString();
	}
}
