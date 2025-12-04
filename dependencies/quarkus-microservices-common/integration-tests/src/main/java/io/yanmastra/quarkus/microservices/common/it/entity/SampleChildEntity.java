package io.yanmastra.quarkus.microservices.common.it.entity;

import io.yanmastra.quarkus.microservices.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_simple_child")
public class SampleChildEntity extends BaseEntity {
    @Id
    @Column(nullable = false, length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "name", length = 36)
    public String name;

    @ManyToMany
    @JoinTable(name = "sample_children",
            joinColumns = @JoinColumn(name = "child_id"),
            inverseJoinColumns = @JoinColumn(name = "sample_id"))
    public List<SampleEntity> parents = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
