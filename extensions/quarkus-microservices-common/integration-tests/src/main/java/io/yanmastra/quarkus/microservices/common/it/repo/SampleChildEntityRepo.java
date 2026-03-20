package io.yanmastra.quarkus.microservices.common.it.repo;

import io.yanmastra.quarkus.microservices.common.it.entity.SampleChildEntity;
import io.yanmastra.quarkus.microservices.common.repository.BaseRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleChildEntityRepo extends BaseRepository<SampleChildEntity, String> {
}
