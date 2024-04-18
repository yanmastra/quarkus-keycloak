package org.acme.microservices.common.reactive.it.repo;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.microservices.common.reactive.it.entity.SampleEntity;

@ApplicationScoped
public class SampleEntityRepositoryReactive implements PanacheRepositoryBase<SampleEntity, String> {
}
