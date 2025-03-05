package io.onebyone.quarkus.microservices.common.crud;

import io.onebyone.quarkus.microservices.common.dto.BaseDto;
import io.onebyone.quarkus.microservices.common.entity.BaseEntity;
import io.onebyone.quarkus.microservices.common.repository.BaseRepository;

/**
 * You can extend this abstract class to your Resource class to implement Pagination Process (Create Read Update and Delete) activity with non-reactive approach,
 * you should provide 2 generic class parameter
 * @param <Entity> is entity class that extend CrudableEntity
 * @param <Dto> is a Data Access Object class like json representation of the Entity class, you can use your entity class itself if it doesn't have any DAO class
 */
public abstract class BasePaginationResource<Entity extends BaseEntity, Dto extends BaseDto<Entity>> extends io.onebyone.quarkus.microservices.common.v2.crud.BasePaginationResource<Entity, Dto, String> {
    @Override
    protected abstract BaseRepository<Entity, String> getRepository();
    @Override
    protected String toId(String id) {
        return id;
    }
}
