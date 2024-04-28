package io.yanmastra.microservices.restSample.entity;

import io.yanmastra.microservices.common.crud.CrudableEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_sample_child_of_child")
public class SampleChildOfChildEntity extends CrudableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;
    @Column(precision = 14, scale = 2)
    private BigDecimal amount;
    @Column(length = 36, name = "column_1")
    private String column1;
    @Column(length = 36, name = "column_2")
    private String column2;
    @Column(length = 36, name = "column_3")
    private String column3;
    @Column(length = 36, name = "column_4")
    private String column4;
    @Column(length = 36, name = "column_5")
    private String column5;
    @Column(length = 36, name = "column_6")
    private String column6;
    @Column(length = 36, name = "column_7")
    private String column7;
    /**
     * This is sample of how we relate 2 tables one to many, in this case we need to relate one record of SampleChildEntity
     * relate to many records of SampleChildOfChildEntity
     * this column would be a foreign key to id of SampleChildEntity,
     */
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = false)
    private SampleChildEntity parent;

    public SampleChildOfChildEntity() {
    }

    public SampleChildOfChildEntity(String id, BigDecimal amount, String column1, String column2, String column3, String column4, String column5, String column6, String column7) {
        this.id = id;
        this.amount = amount;
        this.column1 = column1;
        this.column2 = column2;
        this.column3 = column3;
        this.column4 = column4;
        this.column5 = column5;
        this.column6 = column6;
        this.column7 = column7;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColumn1() {
        return column1;
    }

    public void setColumn1(String column1) {
        this.column1 = column1;
    }

    public String getColumn2() {
        return column2;
    }

    public void setColumn2(String column2) {
        this.column2 = column2;
    }

    public String getColumn3() {
        return column3;
    }

    public void setColumn3(String column3) {
        this.column3 = column3;
    }

    public String getColumn4() {
        return column4;
    }

    public void setColumn4(String column4) {
        this.column4 = column4;
    }

    public String getColumn5() {
        return column5;
    }

    public void setColumn5(String column5) {
        this.column5 = column5;
    }

    public String getColumn6() {
        return column6;
    }

    public void setColumn6(String column6) {
        this.column6 = column6;
    }

    public String getColumn7() {
        return column7;
    }

    public void setColumn7(String column7) {
        this.column7 = column7;
    }

    public SampleChildEntity getParent() {
        return parent;
    }

    public void setParent(SampleChildEntity parent) {
        this.parent = parent;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
