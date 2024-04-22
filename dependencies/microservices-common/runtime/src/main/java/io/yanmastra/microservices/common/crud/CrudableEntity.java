package io.yanmastra.microservices.common.crud;

import jakarta.persistence.MappedSuperclass;
import io.yanmastra.microservices.common.entity.BaseEntity;

import java.io.Serializable;

/**
 * This class need to be extends by Entity class that we need to do CRUD process for it
 * this class only provide abstract method getId() and setId() to make the inherited class you have to implement them
 * this class also extends the BaseEntity class because the inherited class of this class need to use all column on
 *      BaseEntity class
 */
@MappedSuperclass
public abstract class CrudableEntity extends BaseEntity implements Serializable {

    public abstract String getId();
    public abstract void setId(String id);
}
