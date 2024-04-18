package org.acme.microservices.common.it.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.microservices.common.it.entity.SampleEntity;

@ApplicationScoped
public class SampleEntityRepository implements PanacheRepositoryBase<SampleEntity, String> {
}
