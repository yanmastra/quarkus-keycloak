package io.onebyone.microservices.restSample.resource;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.onebyone.microservices.restSample.data.entity.SampleCategory;
import io.onebyone.microservices.restSample.data.entity.SampleChildEntity;
import io.onebyone.microservices.restSample.data.entity.SampleChildOfChildEntity;
import io.onebyone.microservices.restSample.data.entity.SampleParentEntity;
import io.onebyone.microservices.restSample.data.dto.SampleParentEntityDto;
import io.onebyone.microservices.restSample.data.dto.SampleParentSummaryDto;
import io.onebyone.microservices.restSample.data.repository.SampleChildEntityRepository;
import io.onebyone.microservices.restSample.data.repository.SampleChildOfChildEntityRepository;
import io.onebyone.microservices.restSample.data.repository.SampleParentEntityRepository;
import io.onebyone.quarkus.microservices.common.crud.CrudableEndpointResource;
import io.onebyone.quarkus.microservices.common.crud.Paginate;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.hibernate.query.Page;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Path("/api/v1/sample_entity")
@SecurityRequirement(name = "Keycloak")
public class SampleEntityEndpointResource extends CrudableEndpointResource<SampleParentEntity, SampleParentEntityDto> {

    @Inject
    SampleParentEntityRepository sampleEntityRepo;
    @Inject
    SampleChildEntityRepository childRepo;
    @Inject
    SampleChildOfChildEntityRepository childOfChildRepo;
    @Inject
    Logger log;

    @Override
    protected PanacheRepositoryBase<SampleParentEntity, String> getRepository() {
        return sampleEntityRepo;
    }

    @Override
    protected SampleParentEntityDto fromEntity(SampleParentEntity entity) {
        return SampleParentEntityDto.fromEntity(entity);
    }

    @Override
    protected SampleParentEntity toEntity(SampleParentEntityDto sampleParentEntityDto) {
        return sampleParentEntityDto.toEntity();
    }

    @Override
    protected SampleParentEntity update(SampleParentEntity entity, SampleParentEntityDto json) {
        entity.setName(json.name);
        entity.setCategory(json.category);
        entity.setPrice(json.price);
        return entity;
    }

    @Override
    protected Set<String> searchAbleColumn() {
        return Set.of("name", "category", "price");
    }

    @GET
    @Path("summary")
    @RunOnVirtualThread
    public Paginate<SampleParentSummaryDto> getSummary(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @Context ContainerRequestContext requestContext
            ) {
        if (page == null) page = 1;
        if (size == null) size = 10;

        return sampleEntityRepo.getParentSummary(Page.page(size, Math.max(0, page -1)), requestContext.getUriInfo().getQueryParameters());
    }


    private final Random random = new Random();
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);
    void seeds(@Observes StartupEvent event) {
        long count = sampleEntityRepo.count();
        log.info("found " + count + " sample entities");
        if (count <= 5000) {
            for (int i = 0; i < 5000; i++) {
                int index = i;
                executorService.submit(() -> createOneParent(index));
            }
        }
        executorService.shutdown();
    }

    @RunOnVirtualThread
    @Transactional
    void createOneParent(int i) {
        Instant start = Instant.now();
        try {
            SampleParentEntity parent = new SampleParentEntity();
            parent.setName("Parent entity " + UUID.randomUUID());
            parent.setCategory(SampleCategory.values()[random.nextInt(SampleCategory.values().length - 1)]);
            parent.setPrice(BigDecimal.valueOf(random.nextInt(1, 100) * 1000L));
            sampleEntityRepo.persist(parent);

            Set<SampleChildEntity> sampleChildEntities = new HashSet<>();
            Set<SampleChildOfChildEntity> sampleChildOfChildEntities = new HashSet<>();
            for (int j = 0; j < random.nextInt(0, 100); j++) {
                SampleChildEntity entity = new SampleChildEntity();
                entity.setName("Child " + i);
                entity.setDescription("This is the sample of Child entity " + i);
                entity.setParent(parent);
                sampleChildEntities.add(entity);

                for (int k = 0; k < random.nextInt(3, 100); k++) {
                    SampleChildOfChildEntity childOfChild = new SampleChildOfChildEntity();
                    BigDecimal amount = BigDecimal.valueOf(random.nextInt(1, 100) * 1000L);
                    childOfChild.setAmount(amount);

                    childOfChild.setColumn1(UUID.randomUUID().toString());
                    childOfChild.setColumn2(UUID.randomUUID().toString());
                    childOfChild.setColumn3(UUID.randomUUID().toString());
                    childOfChild.setColumn4(UUID.randomUUID().toString());
                    childOfChild.setColumn5(UUID.randomUUID().toString());
                    childOfChild.setColumn6(UUID.randomUUID().toString());
                    childOfChild.setColumn6(UUID.randomUUID().toString());

                    childOfChild.setParent(entity);
                    sampleChildOfChildEntities.add(childOfChild);
                }
            }

//            log.warn(+sampleChildEntities.size() + " child created!");
            childRepo.persist(sampleChildEntities.stream());
            childOfChildRepo.persist(sampleChildOfChildEntities.stream());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
        log.warn(i +" takes time: "+(Instant.now().toEpochMilli() - start.toEpochMilli())+"ms");
        if (sampleEntityRepo.count() > 10000) {
            throw new RuntimeException("Stopping loop");
        }
    }
}
