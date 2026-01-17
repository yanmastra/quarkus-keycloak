package io.yanmastra.quarkus.microservices.common.it.entity;

import io.yanmastra.quarkus.microservices.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_simple")
public class SampleEntity extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, length = 36)
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

    @Column(name = "x_date")
    public LocalDate date;;

    @Column(name = "x_date_time")
    public OffsetDateTime dateTime;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "sample_children",
            joinColumns = @JoinColumn(name = "sample_id"),
            inverseJoinColumns = @JoinColumn(name = "child_id"),
            uniqueConstraints = @UniqueConstraint(name = "_unq_sample_child", columnNames = {"sample_id", "child_id"})
    )
    public List<SampleChildEntity> children = new ArrayList<>();

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
