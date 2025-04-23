package io.onebyone.microservices.restSample.resource;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.onebyone.microservices.restSample.data.entity.SampleChildOfChildEntity;
import io.onebyone.microservices.restSample.dto.SampleChildOfChildEntityDto;
import io.onebyone.microservices.restSample.data.repository.SampleChildOfChildEntityRepository;
import io.onebyone.quarkus.microservices.common.crud.CrudableEndpointResource;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@Path("api/v1/child_of_child")
@SecurityRequirement(name = "Keycloak")
public class SampleChildOfChildEndpointResource extends CrudableEndpointResource<SampleChildOfChildEntity, SampleChildOfChildEntityDto> {
    @Inject
    SampleChildOfChildEntityRepository repository;

    @Override
    protected PanacheRepositoryBase<SampleChildOfChildEntity, String> getRepository() {
        return repository;
    }

    @Override
    protected SampleChildOfChildEntityDto fromEntity(SampleChildOfChildEntity entity) {
        return SampleChildOfChildEntityDto.fromEntity(entity);
    }

    @Override
    protected SampleChildOfChildEntity toEntity(SampleChildOfChildEntityDto sampleChildOfChildEntityDto) {
        return sampleChildOfChildEntityDto.toEntity();
    }

    @Override
    protected SampleChildOfChildEntity update(SampleChildOfChildEntity entity, SampleChildOfChildEntityDto sampleChildOfChildEntityDto) {
        return SampleChildOfChildEntityDto.update(entity, sampleChildOfChildEntityDto);
    }
}
