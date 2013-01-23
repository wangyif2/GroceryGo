package com.groceryotg.database;

import javax.persistence.*;

/**
 * User: robert
 * Date: 20/01/13
 */

//Store(store_id, store_name, store_address, store_parent, store_url)

@Entity
@Table(name = "Store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "store_id")
    private int storeId;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_address")
    private String storeAddress;

    //TODO: need to figure out if this is a inner reference
    @Column(name = "store_parent")
    private int storeParent;

    @Column(name = "store_url")
    private String storeUrl;

    public Store(String storeName, String storeAddress, int storeParent, String storeUrl) {
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.storeParent = storeParent;
        this.storeUrl = storeUrl;
    }

    public Store() {
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

    public int getStoreParent() {
        return storeParent;
    }

    public void setStoreParent(int storeParent) {
        this.storeParent = storeParent;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public void setStoreUrl(String storeUrl) {
        this.storeUrl = storeUrl;
    }
}
