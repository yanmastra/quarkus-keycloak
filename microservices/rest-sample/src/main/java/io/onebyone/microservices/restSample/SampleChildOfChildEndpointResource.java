package io.onebyone.microservices.restSample;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.onebyone.microservices.restSample.entity.SampleChildOfChildEntity;
import io.onebyone.microservices.restSample.json.SampleChildOfChildEntityJson;
import io.onebyone.microservices.restSample.repo.SampleChildOfChildEntityRepository;
import io.onebyone.quarkus.microservices.common.crud.CrudableEndpointResource;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

@Path("api/v1/child_of_child")
@SecurityRequirement(name = "Keycloak")
public class SampleChildOfChildEndpointResource extends CrudableEndpointResource<SampleChildOfChildEntity, SampleChildOfChildEntityJson> {
    @Inject
    SampleChildOfChildEntityRepository repository;

    @Override
    protected PanacheRepositoryBase<SampleChildOfChildEntity, String> getRepository() {
        return repository;
    }

    @Override
    protected SampleChildOfChildEntityJson fromEntity(SampleChildOfChildEntity entity) {
        return SampleChildOfChildEntityJson.fromEntity(entity);
    }

    @Override
    protected SampleChildOfChildEntity toEntity(SampleChildOfChildEntityJson sampleChildOfChildEntityJson) {
        return sampleChildOfChildEntityJson.toEntity();
    }

    @Override
    protected SampleChildOfChildEntity update(SampleChildOfChildEntity entity, SampleChildOfChildEntityJson sampleChildOfChildEntityJson) {
        return SampleChildOfChildEntityJson.update(entity, sampleChildOfChildEntityJson);
    }
}
