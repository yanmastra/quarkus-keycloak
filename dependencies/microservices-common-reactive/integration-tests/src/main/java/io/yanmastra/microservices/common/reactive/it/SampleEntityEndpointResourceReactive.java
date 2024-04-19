package io.yanmastra.microservices.common.reactive.it;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import io.yanmastra.microservices.common.reactive.crud.CrudableEndpointResourceReactive;
import io.yanmastra.microservices.common.reactive.it.entity.SampleEntity;
import io.yanmastra.microservices.common.reactive.it.json.SampleEntityJson;
import io.yanmastra.microservices.common.reactive.it.repo.SampleEntityRepositoryReactive;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import java.util.Set;

@Path("/api/v1/sampleEntity-reactive")
@SecurityRequirement(name = "Keycloak")
public class SampleEntityEndpointResourceReactive extends CrudableEndpointResourceReactive<SampleEntity, SampleEntityJson> {

    @Inject
    SampleEntityRepositoryReactive sampleEntityRepo;

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
    protected Uni<SampleEntity> update(SampleEntity entity, SampleEntityJson sampleEntityJson) {
        return Uni.createFrom().item(sampleEntityJson)
                .map(json -> {
                    entity.name = json.name;
                    entity.category = json.category;
                    entity.price = json.price;
                    return entity;
                });
    }

    @Override
    protected Set<String> searchAbleColumn() {
        return Set.of("name", "category", "price");
    }
}
