package com.grocerygo.android.database.objects;

public class Flyer {
	private Integer flyerId;

	private String flyerUrl;
	
	private StoreParent storeParent;

	public Flyer(String flyerUrl, StoreParent flyerStoreParent) {
		this.flyerUrl = flyerUrl;
		this.storeParent = flyerStoreParent;
	}

	public Flyer() {
	}

	public int getFlyerId() {
		return flyerId;
	}

	public void setFlyerId(int flyerId) {
		this.flyerId = flyerId;
	}

	public String getUrl() {
		return flyerUrl;
	}

	public void setUrl(String flyerUrl) {
		this.flyerUrl = flyerUrl;
	}
	
	public StoreParent getStoreParent() {
		return storeParent;
	}
	
	public void setStoreParent(StoreParent flyerStoreParent) {
		this.storeParent = flyerStoreParent;
	}

}
