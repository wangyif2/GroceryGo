package com.groceryotg.database;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.Types;
import java.util.Date;

/**
 * User: robert
 * Date: 20/01/13
 */

//Grocery (_id, item_id, raw_string, unit_price, unit_type_id, total_price, start_date, end_date, line_number, store_id, update_date)

@Entity //tell hibernate to treat this class as entity and save it, refer to the config file
@Table(name = "Grocery")
public class Grocery {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "grocery_id")
    private int groceryId;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "total_price")
    private float totalPrice;

    @Column(name = "unit_price")
    private long unitPrice;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_ate")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "update_date")
    @Temporal(TemporalType.DATE)
    private Date updateDate;

    @Column(name = "line_number")
    private int lineNumber;

    @Column(name = "raw_string")
    @Lob
    private String rawString;

    public Grocery(Store store, long totalPrice, long unitPrice, Unit unit, Date startDate, Date endDate, Date updateDate, int lineNumber, String rawString) {
        this.store = store;
        this.totalPrice = totalPrice;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.startDate = startDate;
        this.endDate = endDate;
        this.updateDate = updateDate;
        this.lineNumber = lineNumber;
        this.rawString = rawString;
    }

    public Grocery() {
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(long unitPrice) {
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

    public int getLineNumber() {
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
}
