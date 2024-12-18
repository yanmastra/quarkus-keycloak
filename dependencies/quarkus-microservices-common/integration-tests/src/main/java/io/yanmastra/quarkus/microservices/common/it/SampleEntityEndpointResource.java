package io.yanmastra.quarkus.microservices.common.it;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.yanmastra.quarkus.microservices.common.crud.CrudableEndpointResource;
import io.yanmastra.quarkus.microservices.common.it.entity.SampleEntity;
import io.yanmastra.quarkus.microservices.common.it.json.SampleEntityJson;
import io.yanmastra.quarkus.microservices.common.it.repo.SampleEntityRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
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
