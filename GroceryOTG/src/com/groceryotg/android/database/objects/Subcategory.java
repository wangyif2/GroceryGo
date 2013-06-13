package com.groceryotg.android.database.objects;

public class Subcategory {
    private Integer subcategoryId;
    private String subcategoryName;
    private String subcategoryTags;
    private Category categoryId;

    public Subcategory(String subcategoryName, String subcategoryTags, Category categoryId) {
        this.subcategoryName = subcategoryName;
        this.subcategoryTags = subcategoryTags;
        this.categoryId = categoryId;
    }

    public Subcategory() {
    }

    public String getSubcategoryName() {
        return subcategoryName;
    }

    public void setSubcategoryName(String subcategoryName) {
        this.subcategoryName = subcategoryName;
    }

    public String getSubcategoryTags() {
        return subcategoryTags;
    }

    public void setSubcategoryTags(String subcategoryTags) {
        this.subcategoryTags = subcategoryTags;
    }

    public Category getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Category categoryId) {
        this.categoryId = categoryId;
    }

    public int getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(int subcategoryId) {
        this.subcategoryId = subcategoryId;
    }
}
