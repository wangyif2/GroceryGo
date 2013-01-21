package com.groceryotg.database;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * User: robert
 * Date: 20/01/13
 */

@Entity //tell hibernate to treat this class as entity and save it, refer to the config file
public class Grocery {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int groceryId;

    private String rawString;

    public Grocery(String rawString) {
        this.rawString = rawString;
    }

    public Grocery() {
    }

    public int getGroceryId() {
        return groceryId;
    }

    public void setGroceryId(int groceryId) {
        this.groceryId = groceryId;
    }

    public String getRawString() {
        return rawString;
    }

    public void setRawString(String rawString) {
        this.rawString = rawString;
    }
}
