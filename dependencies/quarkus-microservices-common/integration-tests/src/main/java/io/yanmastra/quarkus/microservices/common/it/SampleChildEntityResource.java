package io.yanmastra.quarkus.microservices.common.it;

import io.yanmastra.quarkus.microservices.common.crud.CrudableEndpointResource;
import io.yanmastra.quarkus.microservices.common.it.entity.SampleChildEntity;
import io.yanmastra.quarkus.microservices.common.it.entity.SampleEntity;
import io.yanmastra.quarkus.microservices.common.it.json.SampleChildEntityJson;
import io.yanmastra.quarkus.microservices.common.it.repo.SampleChildEntityRepo;
import io.yanmastra.quarkus.microservices.common.it.repo.SampleEntityRepository;
import io.yanmastra.quarkus.microservices.common.repository.BaseRepository;
import io.quarkus.panache.common.Sort;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/api/v1/sampleChildEntity")
public class SampleChildEntityResource extends CrudableEndpointResource<SampleChildEntity, SampleChildEntityJson> {

    @Inject
    SampleChildEntityRepo repo;
    @Inject
    SampleEntityRepository entityRepo;

    @Override
    protected SampleChildEntity toEntity(SampleChildEntityJson dto) {
        return dto.toEntity();
    }

    @Override
    protected SampleChildEntity update(SampleChildEntity entity, SampleChildEntityJson dao) {
        return dao.toEntity(entity);
    }

    @Override
    protected BaseRepository<SampleChildEntity, String> getRepository() {
        return repo;
    }

    @Override
    protected SampleChildEntityJson fromEntity(SampleChildEntity entity) {
        return SampleChildEntityJson.toJson(entity);
    }


    @RunOnVirtualThread
    @GET
    @Path("generate")
    @Transactional
    public Response generate(@QueryParam("count") Integer count) {
        if (count == null || count < 1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<SampleChildEntity> generated = new ArrayList<>();
        List<SampleEntity> entities = entityRepo.findAll(Sort.by("createdAt")).list();
        for (int i = 0; i < count; i++) {
            SampleChildEntity sampleEntity = new SampleChildEntity();
            sampleEntity.name = "Something child "+i;

            int addParent = SampleEntityEndpointResource.random.nextInt(1, 10);
            for (int j = 0; j < addParent; j++) {
                int parentIndex = SampleEntityEndpointResource.random.nextInt(0, entities.size()-1);
                sampleEntity.parents.add(entities.get(parentIndex));
            }

            generated.add(sampleEntity);
        }

        getRepository().persist(generated);
        return Response.ok().build();
    }
}
