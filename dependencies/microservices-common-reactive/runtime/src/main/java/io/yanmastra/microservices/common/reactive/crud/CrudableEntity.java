package io.yanmastra.microservices.common.reactive.crud;

import jakarta.persistence.MappedSuperclass;
import io.yanmastra.microservices.common.reactive.entity.BaseEntity;

import java.io.Serializable;

@MappedSuperclass
public abstract class CrudableEntity extends BaseEntity implements Serializable {

    public abstract String getId();
    public abstract void setId(String id);
}
