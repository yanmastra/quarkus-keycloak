package io.yanmastra.quarkus.microservices.common.crud;

import io.yanmastra.quarkus.microservices.common.dto.BaseDto;
import io.yanmastra.quarkus.microservices.common.entity.BaseEntity;

/**
 * You can extend this abstract class to your Resource class to implement CRUD (Create Read Update and Delete) activity with non-reactive approach,
 * you should provide 2 generic class parameter
 * @param <Entity> is entity class that extend CrudableEntity
 * @param <Dto> is a Data Access Object class like json representation of the Entity class, you can use your entity class itself if it doesn't have any DAO class
 */
public abstract class CrudableEndpointResource<Entity extends BaseEntity, Dto extends BaseDto<Entity>> extends io.yanmastra.quarkus.microservices.common.v2.crud.CrudableEndpointResource<Entity, Dto, String> {

    @Override
    protected String toId(String id) {
        return id;
    }
}
