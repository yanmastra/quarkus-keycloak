package io.yanmastra.microservices.restSample.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.yanmastra.microservices.restSample.entity.SampleChildEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleChildEntityRepository implements PanacheRepositoryBase<SampleChildEntity, String> {
}
