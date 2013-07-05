package com.grocerygo.android.database.objects;

import java.util.Date;

public class Grocery {
	private Integer groceryId;

	private Item item;

	private Flyer flyer;

	private Double totalPrice;

	private Double unitPrice;

	private Unit unit;

	private Date startDate;

	private Date endDate;

	private Date updateDate;

	private Integer lineNumber;

	private Double score;

	private String rawString;

	public Grocery(Item item, Flyer flyer, Double totalPrice, Double unitPrice, Unit unit, Date startDate, Date endDate, Date updateDate, Integer lineNumber, Double sc
			, String rawString) {
		this.item = item;
		this.flyer = flyer;
		this.totalPrice = totalPrice;
		this.unitPrice = unitPrice;
		this.unit = unit;
		this.startDate = startDate;
		this.endDate = endDate;
		this.updateDate = updateDate;
		this.lineNumber = lineNumber;
		this.score = sc;
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

	public Flyer getFlyer() {
		return flyer;
	}

	public void setFlyer(Flyer flyer) {
		this.flyer = flyer;
	}

	public Double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public Double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getRawString() {
		return rawString;
	}

	public void setRawString(String rawString) {
		this.rawString = rawString;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Integer getCategoryId() {
		return this.getItem().getSubcategory().getCategoryId().getCategoryId();
	}

}
