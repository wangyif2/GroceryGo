package com.groceryotg.database;

import javax.persistence.*;

/**
 * User: robert
 * Date: 20/01/13
 */

//Subcategory(subcategory_id PRIMARY KEY, subcategory_name, category_id, category_name)

@Entity
@Table(name = "Subcategory")
public class Subcategory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "subcategory_id")
    private int subcategoryId;

    @Column(name = "subcategory_name")
    private String subcategoryName;

    //TODO: need to figure out if this is a inter reference
//    @Column(name = "category_id")
//    private int categoryId;

    @Column(name = "category_id")
    private String categoryName;

    public Subcategory(String subcategoryName, int categoryId, String categoryName) {
        this.subcategoryName = subcategoryName;
//        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public Subcategory() {
    }

    public String getSubcategoryName() {
        return subcategoryName;
    }

    public void setSubcategoryName(String subcategoryName) {
        this.subcategoryName = subcategoryName;
    }

//    public int getCategoryId() {
//        return categoryId;
//    }

//    public void setCategoryId(int categoryId) {
//        this.categoryId = categoryId;
//    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
