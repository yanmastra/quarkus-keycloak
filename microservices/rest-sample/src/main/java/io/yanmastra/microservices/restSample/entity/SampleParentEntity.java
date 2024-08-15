package io.yanmastra.microservices.restSample.entity;

import io.yanmastra.quarkus.microservices.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;
import java.util.Set;

/**
 * This is sample of how to create entity class to define a table scheme,
 * this class extends the CrudableEntity to indicate we will do CRUD process for this Entity
 * \@Entity is used to define this class is an Entity, it's mean this class is to define a table schema
 * \@Table is used to customize table name, adding column index, adding constraint, and customize table schema with custom sql
 * \@SQLDelete is used to override real delete operation, in this case, we want to do soft-delete for this table,
 * so when someone call the deleteById(id) method from repository class, the deleted_at column would be automatically filled
 * \@Filter is used to define how do we filter the data when we want to load the data, in this case,
 *      we need to create a filter with name deletedAppFilter and the filter condition is to based on value of deleted_at is null or not
 *      if deleted_at is not null mean the data is deleted, and is it null mean the data is not deleted
 * \@FilterDef is used to define the parameters needed on \@Filter, in this case we need to define :isDeleted parameter for 'deletedAppFilter' filter,
 *      and should be defining the data type of the parameter
 */
@Entity
@Table(name = "tb_sample_parent", indexes = {
        @Index(name = "searchable", columnList = "id, name, category"),
        @Index(name = "countable", columnList = "price"),
})
@SQLDelete(sql = "UPDATE tb_parent_simple SET deleted_at=NOW() WHERE id=?")
@Filter(name = "deletedAppFilter", condition = "deleted_at is not null = :isDeleted")
@FilterDef(name = "deletedAppFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
public class SampleParentEntity extends BaseEntity {

    /**
     * This column will use varchar(36) data type and automatically filled by UUID when the record persisted
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
    private String id;

    /**
     * This column will use varchar(255) data type because there is no 'length' defined
     */
    @Column(name = "name")
    private String name;

    /**
     * This column will use ENUM('CATEGORY_1', 'CATEGORY_2', ...,etc.) data type
     * because the category variable using enum, and \@Enumerated to define how do we store the enum value to database,
     * if not defined, by default it would be stored ordinal of the enum like ENUM(0,1,2,...etc.),
     * if defined as EnumType.STRING, it would be stored name of the enum.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private SampleCategory category;

    /**
     * This column will use DECIMAL(14, 2) ,
     * The precision represents the number of significant digits that are stored for values, and
     * the scale represents the number of digits that can be stored following the decimal point.
     * in this case, the possible values are -999999999999.99 to 999999999999.99 (12 digits before point and 2 digits after point, 12 + 2 = precision value)
     */
    @Column(name = "price", precision = 14, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @OneToMany(mappedBy = "parent")
    private Set<SampleChildEntity> children;

    public SampleParentEntity() {}

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected <Dto> Dto toDto() {
        return null;
    }

    @Override
    public <Dto> void updateByDto(Dto dto) {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SampleCategory getCategory() {
        return category;
    }

    public void setCategory(SampleCategory category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Set<SampleChildEntity> getChildren() {
        return children;
    }
}
