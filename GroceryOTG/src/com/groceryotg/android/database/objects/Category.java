package com.groceryotg.android.database.objects;

/**
 * User: robert
 * Date: 07/02/13
 */
public class Category {
    private Integer categoryId;

    private String categoryName;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Category(){
    }

    public Category(String categoryName) {
        this.categoryName = categoryName;
    }
}
