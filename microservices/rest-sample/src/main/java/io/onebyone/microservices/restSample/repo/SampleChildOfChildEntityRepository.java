package io.onebyone.microservices.restSample.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.onebyone.microservices.restSample.entity.SampleChildOfChildEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleChildOfChildEntityRepository implements PanacheRepositoryBase<SampleChildOfChildEntity, String> {
}
