package io.onebyone.quarkus.microservices.common.it.entity;

import io.onebyone.quarkus.microservices.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_simple")
public class SampleEntity extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    public String id;
    @Column(name = "name")
    public String name;
    @Column(name = "category", length = 36)
    public String category;
    @Column(name = "price", precision = 14, scale = 2)
    public BigDecimal price = BigDecimal.ZERO;
    @Column(name = "is_active")
    public Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "sample_type")
    public SampleType sampleType;

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
}
