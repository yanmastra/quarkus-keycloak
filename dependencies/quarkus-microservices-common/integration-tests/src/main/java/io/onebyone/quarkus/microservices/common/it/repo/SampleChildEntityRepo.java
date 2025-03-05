package io.onebyone.quarkus.microservices.common.it.repo;

import io.onebyone.quarkus.microservices.common.it.entity.SampleChildEntity;
import io.onebyone.quarkus.microservices.common.repository.BaseRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleChildEntityRepo extends BaseRepository<SampleChildEntity, String> {
}
