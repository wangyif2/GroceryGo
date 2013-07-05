package com.grocerygo.android.database.objects;

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

	/**
	 * @return the itemId
	 */
	public Integer getItemId() {
		return itemId;
	}

	/**
	 * @param itemId the itemId to set
	 */
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
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
