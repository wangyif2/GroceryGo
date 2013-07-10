package com.grocerygo.android.fragment;

public class ShopCartSummaryItem {
	private String storeParentId;
	private String storeParentName;
	private String storeTotal;
	
	public ShopCartSummaryItem() {
		
	}
	
	public ShopCartSummaryItem(String storeParentId, String storeParentName, String storeTotal) {
		this.storeParentId = storeParentId;
		this.storeParentName = storeParentName;
		this.storeTotal = storeTotal;
	}
	
	public String getStoreParentId() {
		return this.storeParentId;
	}
	
	public String getStoreParentName() {
		return this.storeParentName;
	}
	
	public String getStoreTotal() {
		return this.storeTotal;
	}
	
	public void setStoreParentId(String storeParentId) {
		this.storeParentId = storeParentId;
	}
	
	public void setStoreParentName(String storeParentName) {
		this.storeParentName = storeParentName;
	}
	
	public void setStoreTotal(String storeTotal) {
		this.storeTotal = storeTotal;
	}
}
