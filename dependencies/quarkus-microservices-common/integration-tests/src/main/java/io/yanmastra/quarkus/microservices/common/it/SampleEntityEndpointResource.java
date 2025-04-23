package io.yanmastra.quarkus.microservices.common.it;

import io.smallrye.common.annotation.RunOnVirtualThread;
import io.yanmastra.quarkus.microservices.common.crud.CrudableEndpointResource;
import io.yanmastra.quarkus.microservices.common.it.entity.SampleChildEntity;
import io.yanmastra.quarkus.microservices.common.it.entity.SampleEntity;
import io.yanmastra.quarkus.microservices.common.it.entity.SampleType;
import io.yanmastra.quarkus.microservices.common.it.json.SampleEntityJson;
import io.yanmastra.quarkus.microservices.common.it.repo.SampleChildEntityRepo;
import io.yanmastra.quarkus.microservices.common.it.repo.SampleEntityRepository;
import io.yanmastra.quarkus.microservices.common.repository.BaseRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Path("/api/v1/sample-entity")
public class SampleEntityEndpointResource extends CrudableEndpointResource<SampleEntity, SampleEntityJson> {

    @Inject
    SampleEntityRepository sampleEntityRepo;
    @Inject
    SampleChildEntityRepo childRepo;

    @Override
    protected BaseRepository<SampleEntity, String> getRepository() {
        return sampleEntityRepo;
    }

    @Override
    protected SampleEntityJson fromEntity(SampleEntity entity) {
        return SampleEntityJson.toJson(entity);
    }

    @Override
    protected SampleEntity toEntity(SampleEntityJson sampleEntityJson) {
        return sampleEntityJson.toEntity();
    }

    @Override
    protected SampleEntity update(SampleEntity entity, SampleEntityJson json) {
        entity.name = json.name;
        entity.category = json.category;
        entity.price = json.price;
        entity.isActive = json.isActive;
        entity.sampleType = json.sampleType;
        return entity;
    }

    @Override
    protected String toId(String id) {
        return id;
    }

    @Override
    protected Set<String> searchAbleColumn() {
        return Set.of("name", "category", "price");
    }

    public static final Random random = new Random();

    @RunOnVirtualThread
    @GET
    @Path("generate")
    @Transactional
    public Response generate(@QueryParam("count") Integer count) {
        if (count == null || count < 1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<SampleEntity> generated = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            SampleEntity sampleEntity = new SampleEntity();
            sampleEntity.category = "CATEGORY_" + (i%4);
            sampleEntity.isActive = (i%2==1);
            sampleEntity.name = "Something "+i;
            sampleEntity.price = BigDecimal.valueOf(random.nextInt(99999) * (i%5))
                    .divide(new BigDecimal((random.nextInt(1, 9) * (i%7+1)) + ""), 2, RoundingMode.HALF_EVEN);
            sampleEntity.sampleType = SampleType.values()[random.nextInt(SampleType.values().length)];
            sampleEntity.date = new Date();
            generated.add(sampleEntity);
        }

        getRepository().persist(generated);
        return Response.ok().build();
    }

    @RunOnVirtualThread
    @GET
    @Path("test-query")
    public List<SampleEntityJson> query(
            @QueryParam("child") String ids
    ){
        SampleChildEntity child = childRepo.findById("17067fce-01a9-494a-a379-03319ab14fd0");
        List<SampleEntity> result = sampleEntityRepo.find("where :child in elements(children)",
                Map.of("child", child)
        ).list();
        return result.stream().map(SampleEntityJson::toJson).toList();
    }
}
