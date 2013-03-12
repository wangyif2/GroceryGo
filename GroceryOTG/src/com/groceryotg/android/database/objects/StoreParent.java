package com.groceryotg.android.database.objects;

public class StoreParent {
    private Integer storeParentId;

    private String storeParentName;

    public StoreParent(String storeParentName) {
        this.storeParentName = storeParentName;
    }

    public StoreParent() {
    }

    public int getStoreParentId() {
        return storeParentId;
    }

    public void setStoreParentId(int storeParentId) {
        this.storeParentId = storeParentId;
    }

    public String getName() {
        return storeParentName;
    }

    public void setName(String storeParentName) {
        this.storeParentName = storeParentName;
    }

}
