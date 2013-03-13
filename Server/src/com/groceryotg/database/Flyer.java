package com.groceryotg.database;

import javax.persistence.*;

/**
 * User: robert
 * Date: 20/01/13
 */

//Flyer(flyer_id, flyer_url, store_parent_id)

@Entity
@Table(name = "Flyer")
public class Flyer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "flyer_id")
    private Integer flyerId;

    @Column(name = "flyer_url")
    private String flyerUrl;

    @Column(name = "store_parent_id")
    private Integer storeParentId;

    public Flyer(String flyerUrl, int storeParentId) {
        this.flyerUrl = flyerUrl;
        this.storeParentId = storeParentId;
    }

    public Flyer() {
    }
    
    public String getFlyerUrl() {
        return flyerUrl;
    }

    public void setFlyerUrl(String flyerUrl) {
        this.flyerUrl = flyerUrl;
    }
    
    public int getStoreParentId() {
        return storeParentId;
    }

    public void setStoreParentId(int storeParentId) {
        this.storeParentId = storeParentId;
    }

}
