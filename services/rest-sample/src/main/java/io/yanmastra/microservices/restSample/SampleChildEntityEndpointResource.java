package io.yanmastra.microservices.restSample;

import io.yanmastra.microservices.restSample.data.entity.SampleChildEntity;
import io.yanmastra.microservices.restSample.data.repository.SampleChildEntityRepository;
import io.yanmastra.microservices.restSample.data.repository.SampleParentEntityRepository;
import io.yanmastra.microservices.restSample.dto.SampleChildEntityDto;
import io.yanmastra.quarkus.microservices.common.crud.CrudableEndpointResource;
import io.yanmastra.quarkus.microservices.common.repository.BaseRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@Path("/api/v1/sample_child_entity")
@SecurityRequirement(name = "Keycloak")
public class SampleChildEntityEndpointResource extends CrudableEndpointResource<SampleChildEntity, SampleChildEntityDto> {
    @Inject
    SampleChildEntityRepository repository;
    @Inject
    SampleParentEntityRepository parentRepository;

    @Override
    protected BaseRepository<SampleChildEntity, String> getRepository() {
        return repository;
    }

    @Override
    protected SampleChildEntityDto fromEntity(SampleChildEntity entity) {
        return SampleChildEntityDto.fromEntity(entity);
    }

    @Override
    protected SampleChildEntity toEntity(SampleChildEntityDto sampleChildEntityDto) {
        return sampleChildEntityDto.toEntity();
    }

    @Override
    protected SampleChildEntity update(SampleChildEntity entity, SampleChildEntityDto sampleChildEntityDto) {
        entity.setName(sampleChildEntityDto.getName());
        entity.setDescription(sampleChildEntityDto.getDescription());
        entity.setParent(parentRepository.findById(sampleChildEntityDto.getParentId()));
        return entity;
    }
}
