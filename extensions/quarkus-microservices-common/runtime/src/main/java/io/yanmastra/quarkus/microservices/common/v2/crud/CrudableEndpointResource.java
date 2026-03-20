package io.yanmastra.quarkus.microservices.common.v2.crud;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.ext.web.handler.HttpException;
import io.yanmastra.quarkus.microservices.common.crud.WriteType;
import io.yanmastra.quarkus.microservices.common.v2.dto.BaseDto;
import io.yanmastra.quarkus.microservices.common.v2.entity.BaseEntity;
import io.yanmastra.quarkusBase.ResponseJson;
import io.yanmastra.quarkusBase.security.UserPrincipal;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.commons.lang3.StringUtils;

/**
 * You can extend this abstract class to your Resource class to implement CRUD (Create Read Update and Delete) activity with non-reactive approach,
 * you should provide 2 generic class parameter
 * @param <Entity> is entity class that extend CrudableEntity
 * @param <Dto> is a Data Access Object class like json representation of the Entity class, you can use your entity class itself if it doesn't have any DAO class
 */
public abstract class CrudableEndpointResource<Entity extends BaseEntity<Id>, Dto extends BaseDto<Entity, Id>, Id> extends BasePaginationResource<Entity, Dto, Id> {
    /**
     * Implement this method to convert Dao object to Entity object
     * @param dao is object of Dao
     * @return object of Entity
     */
    protected abstract Entity toEntity(Dto dao);

    /**
     * Implement this method to pass new attributes value to existed Entity,
     * @param entity is existed entity that ever been persisted before
     * @param dao is a Dao object that contain the data received from request body json
     * @return Entity object from parameter
     */
    protected abstract Entity update(Entity entity, Dto dao);

    @RunOnVirtualThread
    @POST
    @Transactional
    public ResponseJson<Dto> create(Dto dao, @Context SecurityContext context) throws Exception {
        Entity entity = toEntity(dao);

        if (entity.getId() != null) {
            Entity existed = getRepository().findById(entity.getId());
            if (existed != null) {
                throw new HttpException(HttpResponseStatus.CONFLICT.code(), "Same entity already exists!");
            }
        }

        getRepository().persist(entity);
        Dto dao1 = fromEntity(entity);
        dao1.setCreatedBy(null);
        onWriteSuccess(dao1, (UserPrincipal) context.getUserPrincipal(), WriteType.CREATE);

        return new ResponseJson<>(
                true,
                null,
                dao1
        );
    }

    protected Entity findExistingEntity(String id) {
        if (StringUtils.isBlank(id)) {
            throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "id is required");
        }

        Id idID = toId(id);
        Entity existed = getRepository().findActiveById(idID);
        if (existed == null) throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "Unable to find entity with id:"+id);
        return existed;
    }

    @RunOnVirtualThread
    @PUT
    @Path("{id}")
    @Transactional
    public ResponseJson<Dto> update(
            @PathParam("id") String id,
            Dto dao,
            @Context SecurityContext context
    ) throws Exception {
        Entity existed = findExistingEntity(id);
        Entity entity = update(existed, dao);
        entity.setCreatedBy(dao.getCreatedBy());
        getRepository().persist(entity);
        Dto dao1 = fromEntity(entity);

        onWriteSuccess(dao1, (UserPrincipal) context.getUserPrincipal(), WriteType.UPDATE);
        return new ResponseJson<>(
                true, null,
                dao1
        );
    }

    @RunOnVirtualThread
    @DELETE
    @Path("{id}")
    @Transactional
    public ResponseJson<Dto> delete(
            @PathParam("id") String id,
            @Context SecurityContext context
    ) throws Exception {
        Entity existed = findExistingEntity(id);
        boolean result = getRepository().deleteById(toId(id));
        if (result) {
            Dto dao = fromEntity(existed);
            onWriteSuccess(dao, (UserPrincipal) context.getUserPrincipal(), WriteType.DELETE);
            return new ResponseJson<>(true, dao.getClass().getSimpleName() + ":"+id+" has been deleted successfully");
        } else {
            throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "Something wrong when deleting "+existed.getClass().getSimpleName()+":"+id);
        }
    }

    /**
     * You can override this method when you need to do something after creating, updating, or deleting the data,
     * such as sending the data to Message broker os something else,
     * @param dao is Data Access Object that represent the related entity that has been created or modified
     * @param principal is user information who done the process
     * @param type is the types of writing process, they can be CREATED, UPDATE, or DELETE
     * @throws Exception can be thrown if you need to cancel the process
     */
    protected void onWriteSuccess(Dto dao, UserPrincipal principal, WriteType type) throws Exception {
    }
}
