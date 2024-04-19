package io.yanmastra.microservices.common.it.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import io.yanmastra.microservices.common.it.entity.SampleEntity;

@ApplicationScoped
public class SampleEntityRepository implements PanacheRepositoryBase<SampleEntity, String> {
}
