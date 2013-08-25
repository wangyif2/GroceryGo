package ca.grocerygo.database;

import javax.persistence.*;
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
    private Integer groceryId;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "raw_string")
    @Lob
    private String rawString;

    @Column(name = "raw_price")
    private String rawPrice;

    @Column(name = "unit_price", precision = 10,length = 2)
    private Double unitPrice;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "total_price", precision = 10, length = 2)
    private Double totalPrice;

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @ManyToOne
    @JoinColumn(name = "flyer_id")
    private Flyer flyer;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "update_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Column(name = "score")
    private Double score;

    public Grocery(Item item, String rawString, String rawPrice, Double unitPrice, Unit unit, Double totalPrice, Date startDate, Date endDate, Flyer flyer, Integer lineNumber, Date updateDate, Double score) {
        this.item = item;
        this.rawString = rawString;
        this.rawPrice = rawPrice;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.totalPrice = totalPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.flyer = flyer;
        this.lineNumber = lineNumber;
        this.updateDate = updateDate;
        this.score = score;
    }

    public Grocery() {
    }

    public String getRawPrice() {
        return rawPrice;
    }

    public void setRawPrice(String rawPrice) {
        this.rawPrice = rawPrice;
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

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getUnitPrice() {
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
}
