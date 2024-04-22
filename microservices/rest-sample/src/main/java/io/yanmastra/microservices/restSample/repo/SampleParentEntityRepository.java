package io.yanmastra.microservices.restSample.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import io.yanmastra.microservices.restSample.entity.SampleParentEntity;

@ApplicationScoped
public class SampleParentEntityRepository implements PanacheRepositoryBase<SampleParentEntity, String> {
}
