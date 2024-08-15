package io.yanmastra.microservices.restSample;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.yanmastra.microservices.restSample.entity.SampleChildEntity;
import io.yanmastra.microservices.restSample.json.SampleChildEntityJson;
import io.yanmastra.microservices.restSample.repo.SampleChildEntityRepository;
import io.yanmastra.microservices.restSample.repo.SampleParentEntityRepository;
import io.yanmastra.quarkus.microservices.common.crud.CrudableEndpointResource;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@Path("/api/v1/sample_child_entity")
@SecurityRequirement(name = "Keycloak")
public class SampleChildEntityEndpointResource extends CrudableEndpointResource<SampleChildEntity, SampleChildEntityJson> {
    @Inject
    SampleChildEntityRepository repository;
    @Inject
    SampleParentEntityRepository parentRepository;

    @Override
    protected PanacheRepositoryBase<SampleChildEntity, String> getRepository() {
        return repository;
    }

    @Override
    protected SampleChildEntityJson fromEntity(SampleChildEntity entity) {
        return SampleChildEntityJson.fromEntity(entity);
    }

    @Override
    protected SampleChildEntity toEntity(SampleChildEntityJson sampleChildEntityJson) {
        return sampleChildEntityJson.toEntity();
    }

    @Override
    protected SampleChildEntity update(SampleChildEntity entity, SampleChildEntityJson sampleChildEntityJson) {
        entity.setName(sampleChildEntityJson.getName());
        entity.setDescription(sampleChildEntityJson.getDescription());
        entity.setParent(parentRepository.findById(sampleChildEntityJson.getParentId()));
        return entity;
    }
}
