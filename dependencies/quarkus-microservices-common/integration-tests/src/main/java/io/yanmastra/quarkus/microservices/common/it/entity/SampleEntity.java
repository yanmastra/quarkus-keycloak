package io.yanmastra.quarkus.microservices.common.it.entity;

import io.yanmastra.quarkus.microservices.common.entity.BaseEntity;
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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
