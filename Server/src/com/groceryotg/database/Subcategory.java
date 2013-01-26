package com.groceryotg.database;

import javax.persistence.*;

/**
 * User: robert
 * Date: 20/01/13
 */

//Subcategory(subcategory_id PRIMARY KEY, subcategory_name, subcategory_tags TEXT, category_id)

@Entity
@Table(name = "Subcategory")
public class Subcategory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "subcategory_id")
    private int subcategoryId;

    @Column(name = "subcategory_name")
    private String subcategoryName;

    @Column(name = "subcategory_tag")
    private String subcategoryTags;

    @ManyToOne
    @JoinColumn(name = "category_id")
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
