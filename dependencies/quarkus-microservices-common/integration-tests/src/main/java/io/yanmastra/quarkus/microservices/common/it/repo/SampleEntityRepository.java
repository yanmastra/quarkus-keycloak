package io.yanmastra.quarkus.microservices.common.it.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.yanmastra.quarkus.microservices.common.it.entity.SampleEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleEntityRepository implements PanacheRepositoryBase<SampleEntity, String> {
}
