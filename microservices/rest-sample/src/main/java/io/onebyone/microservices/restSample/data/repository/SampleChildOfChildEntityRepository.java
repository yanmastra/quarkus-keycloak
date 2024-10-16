package io.onebyone.microservices.restSample.data.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.onebyone.microservices.restSample.data.entity.SampleChildOfChildEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleChildOfChildEntityRepository implements PanacheRepositoryBase<SampleChildOfChildEntity, String> {
}
