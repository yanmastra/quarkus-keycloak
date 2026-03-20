package io.yanmastra.microservices.restSample.data.repository;

import io.yanmastra.microservices.restSample.data.entity.SampleChildOfChildEntity;
import io.yanmastra.quarkus.microservices.common.repository.BaseRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleChildOfChildEntityRepository extends BaseRepository<SampleChildOfChildEntity, String> {
}
