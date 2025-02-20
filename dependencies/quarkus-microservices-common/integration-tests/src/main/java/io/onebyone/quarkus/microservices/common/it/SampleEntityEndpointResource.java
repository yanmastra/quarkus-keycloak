package io.onebyone.quarkus.microservices.common.it;

import io.onebyone.quarkus.microservices.common.it.entity.SampleType;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.onebyone.quarkus.microservices.common.crud.CrudableEndpointResource;
import io.onebyone.quarkus.microservices.common.it.entity.SampleEntity;
import io.onebyone.quarkus.microservices.common.it.json.SampleEntityJson;
import io.onebyone.quarkus.microservices.common.it.repo.SampleEntityRepository;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
        entity.isActive = json.isActive;
        entity.sampleType = json.sampleType;
        return entity;
    }

    @Override
    protected Set<String> searchAbleColumn() {
        return Set.of("name", "category", "price");
    }

    private static final Random random = new Random();

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
            generated.add(sampleEntity);
        }

        getRepository().persist(generated);
        return Response.ok().build();
    }
}
