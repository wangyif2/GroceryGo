package com.groceryotg.android.database.objects;

/**
 * User: robert
 * Date: 07/02/13
 */
public class Store {
    private Integer storeId;

    private String storeName;

    private String storeAddress;

    private Integer storeParent;

    private String storeUrl;

    public Store(){
    }

    public Store(Integer storeId, String storeName, String storeAddress, Integer storeParent, String storeUrl) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.storeParent = storeParent;
        this.storeUrl = storeUrl;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public Integer getStoreParent() {
        return storeParent;
    }

    public void setStoreParent(Integer storeParent) {
        this.storeParent = storeParent;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public void setStoreUrl(String storeUrl) {
        this.storeUrl = storeUrl;
    }
}
