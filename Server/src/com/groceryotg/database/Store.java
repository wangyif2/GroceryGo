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

    @ManyToOne
    @JoinColumn(name = "store_parent_id")
    private StoreParent storeParent;

    @ManyToOne
    @JoinColumn(name = "flyer_id")
    private Flyer flyer;

    public Store(String storeAddress, Double storeLatitude, Double storeLongitude, StoreParent storeParent, Flyer flyer) {
        this.storeAddress = storeAddress;
        this.storeLatitude = storeLatitude;
        this.storeLongitude = storeLongitude;
        this.storeParent = storeParent;
        this.flyer = flyer;
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
