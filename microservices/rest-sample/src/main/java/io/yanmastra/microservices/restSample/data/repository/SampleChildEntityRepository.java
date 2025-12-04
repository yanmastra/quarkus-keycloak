package io.yanmastra.microservices.restSample.data.repository;

import io.yanmastra.microservices.restSample.data.entity.SampleChildEntity;
import io.yanmastra.quarkus.microservices.common.repository.BaseRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleChildEntityRepository extends BaseRepository<SampleChildEntity, String> {
}
