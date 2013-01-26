package com.groceryotg.database;

import javax.persistence.*;

/**
 * User: robert
 * Date: 24/01/13
 */

//Category(category_id, category_name)

@Entity
@Table(name = "Category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int categoryId;

    @Column(name = "category_name")
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
