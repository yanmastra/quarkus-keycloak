package io.onebyone.microservices.restSample.data.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.onebyone.microservices.restSample.data.entity.SampleChildEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleChildEntityRepository implements PanacheRepositoryBase<SampleChildEntity, String> {
}
