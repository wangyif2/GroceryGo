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

    @ManyToOne
    @JoinColumn(name = "store_parent_id")
    private StoreParent storeParent;

    public Flyer(String flyerUrl, StoreParent storeParent) {
        this.flyerUrl = flyerUrl;
        this.storeParent = storeParent;
    }

    public Flyer() {
    }
    
    public String getFlyerUrl() {
        return flyerUrl;
    }

    public void setFlyerUrl(String flyerUrl) {
        this.flyerUrl = flyerUrl;
    }
    
    public StoreParent getStoreParent() {
        return storeParent;
    }

    public void setStoreParent(StoreParent storeParent) {
        this.storeParent = storeParent;
    }

}
