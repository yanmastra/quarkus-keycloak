package io.yanmastra.quarkus.microservices.common.it.repo;

import io.yanmastra.quarkus.microservices.common.it.entity.SampleEntity;
import io.yanmastra.quarkus.microservices.common.repository.BaseRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleEntityRepository extends BaseRepository<SampleEntity, String> {
}
