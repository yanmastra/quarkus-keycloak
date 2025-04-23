package io.yanmastra.quarkus.microservices.common.entity;

import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

/**
 * This class contains the basic columns that should be defined on every Entity
 */
@MappedSuperclass
public abstract class BaseEntity extends io.yanmastra.quarkus.microservices.common.v2.entities.BaseEntity<String> implements Serializable {
    public abstract String getId();
    public abstract void setId(String id);
}
