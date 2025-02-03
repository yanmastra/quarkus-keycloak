package io.onebyone.quarkus.microservices.common.it.repo;

import io.onebyone.quarkus.microservices.common.it.entity.SampleEntityInteger;
import io.onebyone.quarkus.microservices.common.repository.BaseRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleEntityIntegerRepository extends BaseRepository<SampleEntityInteger, Long> {
}
