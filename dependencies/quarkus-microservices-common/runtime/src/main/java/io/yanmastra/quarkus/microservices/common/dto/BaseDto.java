package io.yanmastra.quarkus.microservices.common.dto;

import java.io.Serializable;

public interface BaseDto<Entity> extends Serializable {
    Entity toEntity();
}
