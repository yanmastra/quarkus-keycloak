package org.acme.microservices.common.it;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.acme.microservices.common.crud.CrudableEndpointResource;
import org.acme.microservices.common.it.entity.SampleEntity;
import org.acme.microservices.common.it.json.SampleEntityJson;
import org.acme.microservices.common.it.repo.SampleEntityRepository;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import java.util.Set;

@Path("/api/v1/sampleEntity")
@SecurityRequirement(name = "Keycloak")
public class SampleEntityEndpointResource extends CrudableEndpointResource<SampleEntity, SampleEntityJson> {

    @Inject
    SampleEntityRepository sampleEntityRepo;

    @Override
    protected PanacheRepositoryBase<SampleEntity, String> getRepository() {
        return sampleEntityRepo;
    }

    @Override
    protected SampleEntityJson fromEntity(SampleEntity entity) {
        return SampleEntityJson.fromJson(entity);
    }

    @Override
    protected SampleEntity toEntity(SampleEntityJson sampleEntityJson) {
        return sampleEntityJson.toJson();
    }

    @Override
    protected SampleEntity update(SampleEntity entity, SampleEntityJson json) {
        entity.name = json.name;
        entity.category = json.category;
        entity.price = json.price;
        return entity;
    }

    @Override
    protected Set<String> searchAbleColumn() {
        return Set.of("name", "category", "price");
    }
}
