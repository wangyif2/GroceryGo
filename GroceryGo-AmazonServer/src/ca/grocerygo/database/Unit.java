package ca.grocerygo.database;

import javax.persistence.*;

/**
 * User: robert
 * Date: 20/01/13
 */

//Unit(unit_id, unit_type_name)

@Entity
@Table(name = "Unit")
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "unit_id")
    private Integer unitId;

    @Column(name = "unit_type_name")
    private String unitTypeName;

    public String getUnitTypeName() {
        return unitTypeName;
    }

    public void setUnitTypeName(String unitTypeName) {
        this.unitTypeName = unitTypeName;
    }

    public Unit() {
    }

    public Unit(String unitTypeName) {
        this.unitTypeName = unitTypeName;
    }

}
