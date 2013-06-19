package com.groceryotg.android.database.objects;

public class Store {
	private Integer storeId;

	private String storeAddress;

	private Double storeLatitude;
	
	private Double storeLongitude;
	
	private StoreParent storeParent;

	private Flyer flyer;

	public Store(){
	}

	public Store(Integer storeId, String storeAddress, Double storeLatitude, Double storeLongitude, StoreParent storeParent, Flyer flyer) {
		this.storeId = storeId;
		this.storeAddress = storeAddress;
		this.storeLatitude = storeLatitude;
		this.storeLongitude = storeLongitude;
		this.storeParent = storeParent;
		this.flyer = flyer;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}

	public String getStoreAddress() {
		return storeAddress;
	}

	public void setStoreAddress(String storeAddress) {
		this.storeAddress = storeAddress;
	}

	public Double getStoreLatitude() {
		return storeLatitude;
	}
	
	public void setStoreLatitude(Double storeLatitude) {
		this.storeLatitude = storeLatitude;
	}
	
	public Double getStoreLongitude() {
		return storeLongitude;
	}
	
	public void setStoreLongitude(Double storeLongitude) {
		this.storeLongitude = storeLongitude;
	}
	
	public StoreParent getStoreParent() {
		return storeParent;
	}

	public void setStoreParent(StoreParent storeParent) {
		this.storeParent = storeParent;
	}
	
	public Flyer getFlyer() {
		return flyer;
	}
	
	public void setFlyer(Flyer flyer) {
		this.flyer = flyer;
	}
}
