package org.acme.microservices.common.crud;

import com.acme.authorization.security.UserPrincipal;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.util.*;

public abstract class CrudEndpoint<Entity extends CrudableEntity, Dao> {
    protected abstract PanacheRepositoryBase<Entity, String> getRepository();
    protected abstract Dao fromEntity(Entity entity);
    protected abstract Entity toEntity(Dao dao);
    protected abstract Uni<Entity> update(Entity entity, Dao dao);

    protected Set<String> searchAbleColumn() {
        return new HashSet<>();
    }

    protected Class<?> searchAbleColumnClass(String columnName) {
        return String.class;
    }

    protected Sort getSort() {
        return Sort.descending("createdAt").and("createdAt", Sort.NullPrecedence.NULLS_LAST);
    }

    @Inject
    Logger logger;

    @GET
    @WithTransaction
    public Uni<Paginate<Dao>> getList(
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
        Uni<Paginate<Dao>> paginateUni;
        if (StringUtils.isBlank(keyword) && otherQueries.isEmpty()) {
            paginateUni = getRepository().count().chain(totalCount -> loadPage(totalCount, getRepository().findAll(sort)
                    .page(objPage.index, objPage.size)
                    .list(), objPage));
        } else {
            Map<String, Object> queryParams = new HashMap<>();
            String query = createFilterQuery(keyword, otherQueries, queryParams);

            logger.debug("query:"+query+", params="+queryParams);
            paginateUni = getRepository().find(
                    query,
                    sort,
                    queryParams
            ).count().chain(totalCount -> loadPage(totalCount, getRepository().find(query, sort, queryParams)
                    .page(objPage.index, objPage.size)
                    .list(), objPage));
        }

        return paginateUni;
    }

    private Uni<Paginate<Dao>> loadPage(long totalCount, Uni<List<Entity>> entities, Page page) {
        return entities.map(list -> list.stream().map(this::fromEntity).toList())
                .map(data -> {
                    long totalPage = totalCount / page.size + (totalCount % page.size == 0 ?  0 : 1);
                    return new Paginate<>(
                            data,
                            page.index + 1,
                            page.size,
                            page.index == 0,
                            (totalPage * page.size) >= totalCount,
                            totalCount,
                            (int) totalPage
                    );
                });
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

    @GET
    @Path("{id}")
    @WithTransaction
    public Uni<Dao> getList(
            @PathParam("id") String id,
            @Context SecurityContext context
    ) {
        return getRepository().findById(id).map(this::fromEntity);
    }

    @POST
    @WithTransaction
    public Uni<Response> create(Dao dao, @Context SecurityContext context) {
        try {
            Entity entity = toEntity(dao);
            return getRepository().persist(entity)
                    .onItem()
                    .transform(entity1 -> {
                        Dao dao1 = fromEntity(entity1);
                        onWriteSuccess(dao1, (UserPrincipal) context.getUserPrincipal(), WriteType.CREATE);

                        Map<String, Object> data = new HashMap<>();
                        data.put("success", true);
                        data.put("data", dao1);
                        return Response.ok(data).build();
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(Response.status(500)
                    .entity(Json.createObjectBuilder().add("success", false)
                            .add("message", e.getMessage())
                            .build()
                            .toString()
                    ).build());
        }
    }

    @PUT
    @Path("{id}")
    @WithTransaction
    public Uni<Response> update(
            @PathParam("id") String id,
            Dao dao,
            @Context SecurityContext context
    ) {
        try {
            Entity entity = toEntity(dao);
            entity.setId(id);
            return getRepository().findById(id)
                    .call(result -> {
                        if (result == null) throw new HttpException(404, "Product:"+id+" not found");
                        else {
                            return update(result, dao)
                                    .call(result1 -> getRepository().persist(result1));
                        }
                    })
                    .onItem()
                    .transform(entity1 -> {
                        Dao dao1 = fromEntity(entity1);
                        onWriteSuccess(dao1, (UserPrincipal) context.getUserPrincipal(), WriteType.UPDATE);
                        Map<String, Object> data = new HashMap<>();
                        data.put("success", true);
                        data.put("data", dao1);
                        return Response.ok(data).build();
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(Response.status(500)
                    .entity(Json.createObjectBuilder().add("success", false)
                            .add("message", e.getMessage())
                            .build()
                            .toString()
                    ).build());
        }
    }

    @DELETE
    @Path("{id}")
    @WithTransaction
    public Uni<Response> delete(
            @PathParam("id") String id,
            @Context SecurityContext context
    ) {
        return getRepository().findById(id)
                        .chain(item -> {
                            if (item != null) {
                                return getRepository().deleteById(id)
                                        .map(result -> {
                                            JsonObject data = Json.createObjectBuilder()
                                                    .add("success", result)
                                                    .add("data", Json.createObjectBuilder().add("id", id).build())
                                                    .build();
                                            if (result) {
                                                onWriteSuccess(fromEntity(item), (UserPrincipal) context.getUserPrincipal(), WriteType.DELETE);
                                                return Response.ok(data.toString()).build();
                                            } else {
                                                return Response.status(500).entity(data.toString()).build();
                                            }
                                        });
                            } else {
                                JsonObject data = Json.createObjectBuilder()
                                        .add("success", false)
                                        .add("message", "Product with id:"+id+" not found!")
                                        .add("data", Json.createObjectBuilder().add("id", id).build())
                                        .build();
                                return Uni.createFrom().item(Response.status(404).entity(data.toString()).build());
                            }
                        });
    }

    protected void onWriteSuccess(Dao dao, UserPrincipal principal, WriteType type) {
    }

}
