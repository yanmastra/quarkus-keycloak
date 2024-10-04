package io.onebyone.microservices.restSample.entity;

import io.onebyone.quarkus.microservices.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "tb_sample_child")
public class SampleChildEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "name", length = 36)
    private String name;

    @Column(name = "description")
    private String description;


    /**
     * This is sample of how do we make a relation between 2 tables by one to many,
     * in this case we need to make one record of SampleParentEntity
     * linked to many records on SampleChildEntity
     * this column would be a foreign key to id of SampleParentEntity,
     */
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = false)
    private SampleParentEntity parent;

    @OneToMany(mappedBy = "parent")
    private Set<SampleChildOfChildEntity> children;

    public SampleChildEntity() {
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SampleParentEntity getParent() {
        return parent;
    }

    public void setParent(SampleParentEntity parent) {
        this.parent = parent;
    }

    public Set<SampleChildOfChildEntity> getChildren() {
        return children;
    }
}
