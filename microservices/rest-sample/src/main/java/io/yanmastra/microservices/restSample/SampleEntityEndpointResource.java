package io.yanmastra.microservices.restSample;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.yanmastra.microservices.common.crud.CrudableEndpointResource;
import io.yanmastra.microservices.restSample.entity.SampleCategory;
import io.yanmastra.microservices.restSample.entity.SampleChildEntity;
import io.yanmastra.microservices.restSample.entity.SampleParentEntity;
import io.yanmastra.microservices.restSample.json.SampleParentEntityJson;
import io.yanmastra.microservices.restSample.repo.SampleChildEntityRepository;
import io.yanmastra.microservices.restSample.repo.SampleParentEntityRepository;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Path("/api/v1/sample_entity")
@SecurityRequirement(name = "Keycloak")
public class SampleEntityEndpointResource extends CrudableEndpointResource<SampleParentEntity, SampleParentEntityJson> {

    @Inject
    SampleParentEntityRepository sampleEntityRepo;
    @Inject
    SampleChildEntityRepository childRepo;
    @Inject
    Logger log;

    @Override
    protected PanacheRepositoryBase<SampleParentEntity, String> getRepository() {
        return sampleEntityRepo;
    }

    @Override
    protected SampleParentEntityJson fromEntity(SampleParentEntity entity) {
        return SampleParentEntityJson.fromEntity(entity);
    }

    @Override
    protected SampleParentEntity toEntity(SampleParentEntityJson sampleParentEntityJson) {
        return sampleParentEntityJson.toEntity();
    }

    @Override
    protected SampleParentEntity update(SampleParentEntity entity, SampleParentEntityJson json) {
        entity.setName(json.name);
        entity.setCategory(json.category);
        entity.setPrice(json.price);
        return entity;
    }

    @Override
    protected Set<String> searchAbleColumn() {
        return Set.of("name", "category", "price");
    }

    private final Random random = new Random();

    @RunOnVirtualThread
    @Transactional
    void seeds(@Observes StartupEvent event) {
        long count = sampleEntityRepo.count();
        if (count <= 10000) {
            for (int i = 0; i < 10000; i++) {

                try {
                    SampleParentEntity parent = new SampleParentEntity();
                    parent.setName("Parent entity " + i);
                    parent.setCategory(SampleCategory.values()[random.nextInt(SampleCategory.values().length - 1)]);
                    parent.setPrice(BigDecimal.valueOf(random.nextInt(1, 100) * 1000L));
                    sampleEntityRepo.persist(parent);

                    Set<SampleChildEntity> sampleChildEntities = new HashSet<>();
                    for (int j = 0; j < random.nextInt(0, 100); j++) {
                        SampleChildEntity entity = new SampleChildEntity();
                        entity.setName("Child " + i);
                        entity.setDescription("This is the sample of Child entity " + i);
                        entity.setParent(parent);
                        sampleChildEntities.add(entity);
                    }

                    log.info(+sampleChildEntities.size() + " child created!");
                    childRepo.persist(sampleChildEntities.stream());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
