package io.yanmastra.microservices.restSample.data.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.yanmastra.microservices.restSample.data.entity.SampleChildEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleChildEntityRepository implements PanacheRepositoryBase<SampleChildEntity, String> {
}
