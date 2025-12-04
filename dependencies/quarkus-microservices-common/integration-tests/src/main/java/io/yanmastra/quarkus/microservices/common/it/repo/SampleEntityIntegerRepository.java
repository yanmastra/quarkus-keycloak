package io.yanmastra.quarkus.microservices.common.it.repo;

import io.yanmastra.quarkus.microservices.common.it.entity.SampleEntityInteger;
import io.yanmastra.quarkus.microservices.common.repository.BaseRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleEntityIntegerRepository extends BaseRepository<SampleEntityInteger, Long> {
}
