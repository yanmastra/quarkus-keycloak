package io.yanmastra.microservices.restSample.data.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.yanmastra.microservices.restSample.data.entity.SampleChildOfChildEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleChildOfChildEntityRepository implements PanacheRepositoryBase<SampleChildOfChildEntity, String> {
}
