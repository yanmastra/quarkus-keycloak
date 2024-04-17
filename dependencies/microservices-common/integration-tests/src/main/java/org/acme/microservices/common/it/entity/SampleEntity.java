package org.acme.microservices.common.it.entity;

import jakarta.persistence.*;
import org.acme.microservices.common.crud.CrudableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_simple")
public class SampleEntity extends CrudableEntity {
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
