package io.yanmastra.quarkus.microservices.common.v2.crud;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.yanmastra.quarkus.microservices.common.crud.Paginate;
import io.yanmastra.quarkus.microservices.common.repository.BaseRepository;
import io.yanmastra.quarkus.microservices.common.utils.CrudQueryFilterUtils;
import io.yanmastra.quarkus.microservices.common.v2.dto.BaseDto;
import io.yanmastra.quarkus.microservices.common.v2.entities.BaseEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.ext.web.handler.HttpException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;

import java.util.*;

/**
 * You can extend this abstract class to your Resource class to implement Pagination Process (Create Read Update and Delete) activity with non-reactive approach,
 * you should provide 2 generic class parameter
 * @param <Entity> is entity class that extend CrudableEntity
 * @param <Dto> is a Data Access Object class like json representation of the Entity class, you can use your entity class itself if it doesn't have any DAO class
 */
public abstract class BasePaginationResource<Entity extends BaseEntity<Id>, Dto extends BaseDto<Entity, Id>, Id> {
    protected static final Logger log = Logger.getLogger(BasePaginationResource.class);

    /**
     * Implement this method and return a Repository object of Entity
     * @return Repository class that extend PanacheRepositoryBase
     */
    protected abstract BaseRepository<Entity, Id> getRepository();

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
     * Deprecated: no longer used because data sorting can be done with the query parameter "?sort=column1:ASC:column2:DESC"
     * @return an object of Sort
     */
    @Deprecated(forRemoval = true, since = "2.3.x")
    protected Sort getSort() {
        return Sort.descending("createdAt").and("createdAt", Sort.NullPrecedence.NULLS_LAST);
    }

    /**
     * Implement this method to convert Entity object to Dao object
     * @param entity is object of Entity
     * @return object of Dao
     */
    protected abstract Dto fromEntity(Entity entity);

    /**
     * Implement this method to convert String path param of {id} to right data type
     * @param id String path param of {id}
     * @return right Id
     */
    protected abstract Id toId(String id);

    @Operation(summary = "Get Paginate data",
            description = """
                    You can add some query parameters based on entity field for filtering, use camel-case format for parameter key, example:
                    <ul>
                        <li>?name=John or ?name=notEquals:Jane</li>
                        <li>?price=lessThan:10000 or ?price=greaterThan:10000 or ?price=range:10000:15000</li>
                        <li>?categoryId=in:CTG_A:CTG_B:CTG_D:CTG_H or ?categoryId=notIn:CTG_A:CTG_B:CTG_D:CTG_H</li>
                        <li>?description=isNull or ?description=isNotNull</li>
                    </ul>
                    Also you can use "?sort=" query parameter for sorting, example:
                    <ul>
                        <li>?sort=name:asc:price:desc</li>
                    </ul>
                    And you can combine them, example:
                    <ul>
                        <li>?price=range:10000:15000&categoryId=in:CTG_A:CTG_B:CTG_D:CTG_H&description=isNotNull&sort=name:asc:price:desc</li>
                    </ul>
                    """
    )
    @RunOnVirtualThread
    @GET
    @Transactional
    public Paginate<Dto> getList(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @QueryParam("sort") String sort,
            @Context ContainerRequestContext context
    ) {
        if (page == null || page <= 0) page = 1;
        if (size == null || size < 5) size = 5;

        MultivaluedMap<String, String> requestQueries = context.getUriInfo().getQueryParameters();
        return this.getList(page, size, requestQueries, context);
    }

    protected Paginate<Dto> getList(Integer page, Integer size, MultivaluedMap<String, String> requestQueries, ContainerRequestContext context) {
        Page objPage = Page.of(page - 1, size);
        Sort sort = CrudQueryFilterUtils.fetchSort(context.getUriInfo().getQueryParameters());

        PanacheQuery<Entity> entityQuery = getRepository().createPaginationQuery(requestQueries, searchAbleColumn(), sort);
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
        Id entityId = toId(id);
        Entity entity = getRepository().find("where id = ?1 and deletedAt is null", entityId).firstResult();
        if (entity != null) {
            return fromEntity(entity);
        }
        throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "Unable to find entity with id:"+id);
    }


    @Operation(summary = "To count the data by specific filter",
            description = """
                    You can add some query parameters based on entity field for filtering, use camel-case format for parameter key, example:
                    <ul>
                        <li>?name=John or ?name=notEquals:Jane</li>
                        <li>?price=lessThan:10000 or ?price=greaterThan:10000 or ?price=range:10000:15000</li>
                        <li>?categoryId=in:CTG_A:CTG_B:CTG_D:CTG_H or ?categoryId=notIn:CTG_A:CTG_B:CTG_D:CTG_H</li>
                        <li>?description=isNull or ?description=isNotNull</li>
                    </ul>
                    And you can combine them, example:
                    <ul>
                        <li>?price=range:10000:15000&categoryId=in:CTG_A:CTG_B:CTG_D:CTG_H&description=isNotNull</li>
                    </ul>
                    """
    )
    @RunOnVirtualThread
    @GET
    @Path("count")
    public Map<String, Long> getCount(@Context ContainerRequestContext context) {
        if (getCountQueries().isEmpty()) return Map.of();

        MultivaluedMap<String, String> requestQueries = context.getUriInfo().getQueryParameters();
        Map<String, Long> result = new HashMap<>();

        for (String key : getCountQueries().keySet()) {
            Map<String, Object> queryParams = new HashMap<>();
            String hql = CrudQueryFilterUtils.createFilterQuery(requestQueries, queryParams, this.searchAbleColumn());

            String query =  getCountQueries().get(key);
            if (StringUtils.isNotBlank(query)) hql += " and " + query;

            Long count = this.getRepository().count(hql, queryParams);
            result.put(key, count);
        }

        return result;
    }


    /**
     * Implement this method to enable /count endpoint
     * @return Map<Key String, HQL Query String
     */
    protected Map<String, String> getCountQueries() {
        return Map.of();
    }

}
