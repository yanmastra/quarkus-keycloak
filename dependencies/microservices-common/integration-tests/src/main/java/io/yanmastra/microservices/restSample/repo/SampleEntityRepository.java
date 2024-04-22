package io.yanmastra.microservices.restSample.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import io.yanmastra.microservices.restSample.entity.SampleEntity;

@ApplicationScoped
public class SampleEntityRepository implements PanacheRepositoryBase<SampleEntity, String> {
}
