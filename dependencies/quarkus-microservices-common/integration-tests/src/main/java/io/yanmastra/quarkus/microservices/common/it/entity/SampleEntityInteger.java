package io.yanmastra.quarkus.microservices.common.it.entity;

import io.yanmastra.quarkus.microservices.common.v2.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "sample_entity_integer")
public class SampleEntityInteger extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 100)
    private String name;
    @Column(length = 100)
    private String description;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long aLong) {
        this.id = aLong;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
