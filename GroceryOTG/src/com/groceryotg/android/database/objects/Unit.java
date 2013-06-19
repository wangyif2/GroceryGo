package com.groceryotg.android.database.objects;

public class Unit {
	private Integer unitId;

	private String unitTypeName;

	public Unit(){
	}

	public Unit(Integer unitId, String unitTypeName) {
		this.unitId = unitId;
		this.unitTypeName = unitTypeName;
	}

	public Integer getUnitId() {
		return unitId;
	}

	public void setUnitId(Integer unitId) {
		this.unitId = unitId;
	}

	public String getUnitTypeName() {
		return unitTypeName;
	}

	public void setUnitTypeName(String unitTypeName) {
		this.unitTypeName = unitTypeName;
	}
}
