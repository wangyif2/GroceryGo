package ca.grocerygo.database;

import javax.persistence.*;

/**
 * User: robert
 * Date: 20/01/13
 */

//StoreParent(store_parent_id, store_parent_name)

@Entity
@Table(name = "StoreParent")
public class StoreParent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "store_parent_id")
    private Integer storeParentId;

    @Column(name = "store_parent_name")
    private String storeParentName;

    public StoreParent(String storeParentName) {
        this.storeParentName = storeParentName;
    }

    public StoreParent() {
    }

    public String getStoreParentName() {
        return storeParentName;
    }

    public void setStoreParentName(String storeParentName) {
        this.storeParentName = storeParentName;
    }
}
