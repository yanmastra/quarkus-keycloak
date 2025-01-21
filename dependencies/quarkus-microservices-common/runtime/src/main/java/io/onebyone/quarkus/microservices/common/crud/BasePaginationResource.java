package io.onebyone.quarkus.microservices.common.crud;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.onebyone.quarkus.microservices.common.dto.BaseDto;
import io.onebyone.quarkus.microservices.common.entity.BaseEntity;
import io.onebyone.quarkus.microservices.common.utils.CrudQueryFilterUtils;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.ext.web.handler.HttpException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;

import java.util.*;

/**
 * You can extend this abstract class to your Resource class to implement Pagination Process (Create Read Update and Delete) activity with non-reactive approach,
 * you should provide 2 generic class parameter
 * @param <Entity> is entity class that extend CrudableEntity
 * @param <Dto> is a Data Access Object class like json representation of the Entity class, you can use your entity class itself if it doesn't have any DAO class
 */
public abstract class BasePaginationResource<Entity extends BaseEntity, Dto extends BaseDto<Entity>> {
    protected static final Logger log = Logger.getLogger(BasePaginationResource.class);

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
    protected abstract Dto fromEntity(Entity entity);

    @Operation(summary = "Get Paginate data",
            description = """
                    Possible parameters:
                    <ul>
                    <li>page: 1, 2, 3, ..., n</li>
                    <li>size: 10, 20, etc.</li>
                    <li>field name of entity class</li>
                    </ul>
                    """
    )
    @RunOnVirtualThread
    @GET
    @Transactional
    public Paginate<Dto> getList(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @Context ContainerRequestContext context
    ) {
        if (page == null || page <= 0) page = 1;
        if (size == null || size < 5) size = 5;

        MultivaluedMap<String, String> requestQueries = context.getUriInfo().getQueryParameters();
        return getList(page, size, requestQueries, context);
    }

    protected Paginate<Dto> getList(Integer page, Integer size, MultivaluedMap<String, String> requestQueries, ContainerRequestContext context) {
        Page objPage = Page.of(page - 1, size);
        Sort sort = getSort();

        Map<String, Object> queryParams = new HashMap<>();
        String hql = CrudQueryFilterUtils.createFilterQuery(requestQueries, queryParams, searchAbleColumn());
        log.debug("generated hql: "+hql);
        log.debug("generated hql value: "+queryParams);

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
    public Dto getOne(
            @PathParam("id") String id,
            @Context SecurityContext context
    ) {
        Entity entity = getRepository().find("where id = ?1 and deletedAt is null", id).firstResult();
        if (entity != null) {
            return fromEntity(entity);
        }
        throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "Unable to find entity with id:"+id);
    }
}
