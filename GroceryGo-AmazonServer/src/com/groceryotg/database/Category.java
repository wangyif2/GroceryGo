package com.groceryotg.database;

import javax.persistence.*;

/**
 * User: robert
 * Date: 24/01/13
 */

@Entity
@Table(name = "Category")
public class Category {

    @Id
    @Column(name = "category_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer categoryId;

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
