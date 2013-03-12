package com.groceryotg.android.database.objects;

public class Flyer {
    private Integer flyerId;

    private String flyerUrl;
    
    private StoreParent flyerStoreParent;

    public Flyer(String flyerUrl, StoreParent flyerStoreParent) {
        this.flyerUrl = flyerUrl;
        this.flyerStoreParent = flyerStoreParent;
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
    	return flyerStoreParent;
    }
    
    public void setStoreParent(StoreParent flyerStoreParent) {
    	this.flyerStoreParent = flyerStoreParent;
    }

}
