package com.groceryotg.database;

import javax.persistence.*;

/**
 * User: robert
 * Date: 20/01/13
 */

//Store(store_id, store_address, store_latitude, store_longitude, store_parent_id, flyer_id)

@Entity
@Table(name = "Store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "store_id")
    private Integer storeId;

    @Column(name = "store_address")
    private String storeAddress;

    @Column(name = "store_latitude")
    private Double storeLatitude;
    
    @Column(name = "store_longitude")
    private Double storeLongitude;
    
    @Column(name = "store_parent_id")
    private Integer storeParentId;

    @Column(name = "flyer_id")
    private Integer flyerId;

    public Store(String storeAddress, Double storeLatitude, Double storeLongitude, int storeParentId, int flyerId) {
        this.storeAddress = storeAddress;
        this.storeLatitude = storeLatitude;
        this.storeLongitude = storeLongitude;
        this.storeParentId = storeParentId;
        this.flyerId = flyerId;
    }

    public Store() {
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
    
    public int getStoreParentId() {
        return storeParentId;
    }

    public void setStoreParentId(int storeParentId) {
        this.storeParentId = storeParentId;
    }

    public int getFlyerId() {
        return flyerId;
    }

    public void setFlyerId(int flyerId) {
        this.flyerId = flyerId;
    }
}
