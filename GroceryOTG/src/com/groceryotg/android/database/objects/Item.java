package com.groceryotg.android.database.objects;

/**
 * User: robert
 * Date: 07/02/13
 */
public class Item {
    private Integer itemId;

    private String itemName;

    private Subcategory subcategory;

    public Item(String itemName, Subcategory subcategory) {
        this.itemName = itemName;
        this.subcategory = subcategory;
    }

    public Item() {
    }


    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Subcategory subcategory) {
        this.subcategory = subcategory;
    }
}
