package io.yanmastra.microservices.common.crud;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.yanmastra.authorization.ResponseJson;
import io.yanmastra.authorization.security.UserPrincipal;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.ext.web.handler.HttpException;
import io.yanmastra.commonClass.utils.CrudQueryFilterUtils;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.util.*;

/**
 * You can extend this abstract class to your Resource class to implement CRUD (Create Read Update and Delete) activity with non-reactive approach,
 * you should provide 2 generic class parameter
 * @param <Entity> is entity class that extend CrudableEntity
 * @param <Dao> is a Data Access Object class like json representation of the Entity class, you can use your entity class itself if it doesn't have any DAO class
 */
public abstract class CrudableEndpointResource<Entity extends CrudableEntity, Dao> {

    /**
     * Implement this method and return a Repository object of Entity
     * @return Repository class that extend PanacheRepositoryBase
     */
    protected abstract PanacheRepositoryBase<Entity, String> getRepository();

    /**
     * Implement this method to convert Entity object to Dao object
     * @param entity is object of Entity
     * @return object of Dao
     */
    protected abstract Dao fromEntity(Entity entity);

    /**
     * Implement this method to convert Dao object to Entity object
     * @param dao is object of Dao
     * @return object of Entity
     */
    protected abstract Entity toEntity(Dao dao);

    /**
     * Implement this method to pass new attributes value to existed Entity,
     * @param entity is existed entity that ever been persisted before
     * @param dao is a Dao object that contain the data received from request body json
     * @return Entity object from parameter
     */
    protected abstract Entity update(Entity entity, Dao dao);

    /**
     * Override this method to provide which columns that can be searched by ``?keyword=`` query parameter
     * <br/>
     * For example, if you have an Entity with column name, category, and description, and you need to search some data based on name or category,
     * you can return Set.of("name", "category");
     * @return Set of String column names;
     */
    protected Set<String> searchAbleColumn() {
        return new HashSet<>();
    }

    /**
     * Override this method to customize the sorting method
     * @return an object of Sort
     */
    protected Sort getSort() {
        return Sort.descending("createdAt").and("createdAt", Sort.NullPrecedence.NULLS_LAST);
    }

    @Inject
    Logger logger;
    @Inject
    CrudQueryFilterUtils queryFilterUtils;


    @RunOnVirtualThread
    @GET
    @Transactional
    public Paginate<Dao> getList(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @Context ContainerRequestContext context
    ) {
        if (page == null || page <= 0) page = 1;
        if (size == null || size < 5) size = 5;
        logger.debug("req: page="+page+", size="+size+", params="+context.getUriInfo().getQueryParameters());

        Page objPage = Page.of(page - 1, size);
        Sort sort = getSort();

        Map<String, Object> queryParams = new HashMap<>();
        String hql = queryFilterUtils.createFilterQuery(context.getUriInfo().getQueryParameters(), queryParams, searchAbleColumn());
        logger.debug("created hql:"+hql);

        PanacheQuery<Entity> entityQuery = getRepository().find(hql, sort, queryParams);
        long totalCount = entityQuery.count();

        List<Entity> result = entityQuery.page(objPage).list();
        return new Paginate<>(
                result.stream().map(this::fromEntity).toList(),
                objPage.index+1,
                objPage.size,
                totalCount
        );
    }

    @RunOnVirtualThread
    @GET
    @Path("{id}")
    @Transactional
    public Dao getList(
            @PathParam("id") String id,
            @Context SecurityContext context
    ) {
        Entity entity = getRepository().find("where id = ?1 and deletedAt is null", id).firstResult();
        if (entity != null) {
            return fromEntity(entity);
        }
        throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "Unable to find entity with id:"+id);
    }

    @RunOnVirtualThread
    @POST
    @Transactional
    public ResponseJson<Dao> create(Dao dao, @Context SecurityContext context) throws Exception {
        Entity entity = toEntity(dao);

        if (StringUtils.isNotBlank(entity.getId())) {
            Entity existed = getRepository().findById(entity.getId());
            if (existed != null) {
                throw new HttpException(HttpResponseStatus.CONFLICT.code(), "Same entity already exists!");
            }
        }

        getRepository().persist(entity);
        Dao dao1 = fromEntity(entity);
        onWriteSuccess(dao1, (UserPrincipal) context.getUserPrincipal(), WriteType.CREATE);

        return new ResponseJson<>(
                true,
                null,
                dao1
        );
    }

    @RunOnVirtualThread
    @PUT
    @Path("{id}")
    @Transactional
    public ResponseJson<Dao> update(
            @PathParam("id") String id,
            Dao dao,
            @Context SecurityContext context
    ) throws Exception {
            Entity existed = getRepository().find("where id = ?1 and deletedAt is null", id).firstResult();
            if (existed == null) throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "Unable to find entity with id:"+id);
            Entity entity = update(existed, dao);
            getRepository().persist(entity);
            Dao dao1 = fromEntity(entity);

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
    public ResponseJson<Dao> delete(
            @PathParam("id") String id,
            @Context SecurityContext context
    ) throws Exception {
        Entity existed = getRepository().find("where id = ?1 and deletedAt is null", id).firstResult();
        if (existed == null) throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "Unable to find entity with id:"+id);

        boolean result = getRepository().deleteById(id);
        if (result) {
            Dao dao = fromEntity(existed);
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
     * @param type is the type write, it can be CREATE, UPDATE, or DELETE
     * @throws Exception can be thrown if you need to cancel the process
     */
    protected void onWriteSuccess(Dao dao, UserPrincipal principal, WriteType type) throws Exception {
    }
}
