package org.acme.microservices.common.crud;

import com.acme.authorization.security.UserPrincipal;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.acme.authorization.json.ResponseJson;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
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

        String keyword = context.getUriInfo().getQueryParameters().getFirst("keyword");
        Map<String, String> otherQueries = new HashMap<>();
        context.getUriInfo().getQueryParameters().entrySet().stream()
                .filter(entry -> !entry.getKey().equals("keyword") && !entry.getKey().equals("page") && !entry.getKey().equals("size"))
                .forEach(entry -> otherQueries.put(entry.getKey(), entry.getValue().get(0)));

        Page objPage = Page.of(page - 1, size);
        Sort sort = getSort();

        logger.debug("req: page="+page+", size="+size+", keyword="+keyword);

        PanacheQuery<Entity> entityQuery;
        if (StringUtils.isBlank(keyword) && otherQueries.isEmpty()) {
            entityQuery = getRepository().findAll(sort);
        } else {
            Map<String, Object> queryParams = new HashMap<>();
            String query = createFilterQuery(keyword, otherQueries, queryParams);

            logger.debug("query:"+query+", params="+queryParams);
            entityQuery = getRepository().find(
                    query,
                    sort,
                    queryParams
            );
        }

        long totalCount = entityQuery.count();
        long totalPage = totalCount / objPage.size + (totalCount % objPage.size == 0 ?  0 : 1);

        List<Entity> result = entityQuery.page(objPage).list();
        return new Paginate<>(
                result.stream().map(this::fromEntity).toList(),
                objPage.index+1,
                objPage.size,
                objPage.index == 0,
                (totalPage * objPage.size) >= totalCount,
                totalCount,
                (int) totalPage
        );
    }

    String createFilterQuery(String keyword, Map<String, String> otherQueries, Map<String, Object> queryParams) {
        String where = "where ";
        StringBuilder sbQuery = new StringBuilder(where);
        if (StringUtils.isNotBlank(keyword)) {
            queryParams.put("keyword", "%"+keyword+"%");
            Set<String> searchKey = new HashSet<>();
            sbQuery.append("(");
            for (String column: searchAbleColumn()) {
                searchKey.add("cast(" + column + " as string) like :keyword");
            }
            sbQuery.append(String.join(" or ", searchKey)).append(")");
        }

        if (StringUtils.isNotBlank(keyword) && !otherQueries.isEmpty()) {
            sbQuery.append(" and ");
        }

        if (!otherQueries.isEmpty()) {
            Iterator<String> keyQueries = otherQueries.keySet().iterator();
            while (keyQueries.hasNext()) {
                String key = keyQueries.next();
                queryParams.put(key, otherQueries.get(key));

                sbQuery.append(key).append("=:").append(key);
                if (keyQueries.hasNext()) sbQuery.append(" and ");
            }
        }
        return sbQuery.toString();
    }

    @RunOnVirtualThread
    @GET
    @Path("{id}")
    @Transactional
    public Dao getList(
            @PathParam("id") String id,
            @Context SecurityContext context
    ) {
        Entity entity = getRepository().findById(id);
        if (entity != null) {
            return fromEntity(entity);
        }
        throw new HttpException(HttpStatus.SC_NOT_FOUND, "Unable to find entity with id:"+id);
    }

    @RunOnVirtualThread
    @POST
    @Transactional
    public ResponseJson<Dao> create(Dao dao, @Context SecurityContext context) throws Exception {
        Entity entity = toEntity(dao);
        Entity existed = getRepository().findById(entity.getId());
        if (existed != null) {
            throw new HttpException(HttpStatus.SC_CONFLICT, "Same entity already exists!");
        }
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
            Entity existed = getRepository().findById(id);
            if (existed == null) throw new HttpException(HttpStatus.SC_NOT_FOUND, "Unable to find entity with id:"+id);
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
        Entity existed = getRepository().findById(id);
        if (existed == null) throw new HttpException(HttpStatus.SC_NOT_FOUND, "Unable to find entity with id:"+id);

        boolean result = getRepository().deleteById(id);
        if (result) {
            Dao dao = fromEntity(existed);
            onWriteSuccess(dao, (UserPrincipal) context.getUserPrincipal(), WriteType.DELETE);
            return new ResponseJson<>(true, dao.getClass().getSimpleName() + ":"+id+" has been deleted successfully");
        } else {
            throw new HttpException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Something wrong when deleting "+existed.getClass().getSimpleName()+":"+id);
        }
    }

    protected void onWriteSuccess(Dao dao, UserPrincipal principal, WriteType type) throws Exception {
    }
}
