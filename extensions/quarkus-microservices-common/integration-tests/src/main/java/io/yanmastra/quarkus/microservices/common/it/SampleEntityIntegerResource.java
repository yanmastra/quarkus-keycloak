package io.yanmastra.quarkus.microservices.common.it;


import io.yanmastra.quarkus.microservices.common.it.entity.SampleEntityInteger;
import io.yanmastra.quarkus.microservices.common.it.json.SampleEntityIntegerDto;
import io.yanmastra.quarkus.microservices.common.it.repo.SampleEntityIntegerRepository;
import io.yanmastra.quarkus.microservices.common.repository.BaseRepository;
import io.yanmastra.quarkus.microservices.common.v2.crud.CrudableEndpointResource;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Path;
import org.apache.commons.lang3.StringUtils;

@Path("api/v1/sample-entity-integer")
public class SampleEntityIntegerResource extends CrudableEndpointResource<SampleEntityInteger, SampleEntityIntegerDto, Long> {

    @Inject
    SampleEntityIntegerRepository repository;

    @Override
    protected SampleEntityInteger toEntity(SampleEntityIntegerDto dao) {
        SampleEntityInteger entity = new SampleEntityInteger();
        entity.setId(dao.getId());
        entity.setName(dao.getName());
        entity.setDescription(dao.getDescription());
        entity.setCreatedBy(dao.getCreatedBy());
        return entity;
    }

    @Override
    protected SampleEntityInteger update(SampleEntityInteger entity, SampleEntityIntegerDto dao) {
        entity.setName(dao.getName());
        entity.setDescription(dao.getDescription());
        entity.setCreatedBy(dao.getCreatedBy());
        return entity;
    }

    @Override
    protected BaseRepository<SampleEntityInteger, Long> getRepository() {
        return repository;
    }

    @Override
    protected SampleEntityIntegerDto fromEntity(SampleEntityInteger entity) {
        SampleEntityIntegerDto dto = new SampleEntityIntegerDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCreatedBy(entity.getCreatedBy());
        return dto;
    }

    @Override
    protected Long toId(String id) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("id is required");
        }
        try {
            return Long.parseLong(id);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
