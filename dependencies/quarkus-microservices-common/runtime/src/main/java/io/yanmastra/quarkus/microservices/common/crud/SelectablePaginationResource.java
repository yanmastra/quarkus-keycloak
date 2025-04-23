package io.yanmastra.quarkus.microservices.common.crud;

import io.yanmastra.quarkus.microservices.common.dto.BaseDto;
import io.yanmastra.quarkus.microservices.common.entity.BaseEntity;

public abstract class SelectablePaginationResource<Entity extends BaseEntity, Dto extends BaseDto<Entity>> extends io.yanmastra.quarkus.microservices.common.v2.crud.SelectablePaginationResource<Entity, Dto, String> {
    @Override
    protected String toId(String id) {
        return id;
    }
}
