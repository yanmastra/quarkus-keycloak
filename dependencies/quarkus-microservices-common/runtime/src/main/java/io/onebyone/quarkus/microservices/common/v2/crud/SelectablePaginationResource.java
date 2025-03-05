package io.onebyone.quarkus.microservices.common.v2.crud;

import io.onebyone.quarkus.microservices.common.crud.Paginate;
import io.onebyone.quarkus.microservices.common.dto.SelectionDto;
import io.onebyone.quarkus.microservices.common.entity.SelectableEntity;
import io.onebyone.quarkus.microservices.common.utils.CrudQueryFilterUtils;
import io.onebyone.quarkus.microservices.common.v2.dto.BaseDto;
import io.onebyone.quarkus.microservices.common.v2.entity.BaseEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SelectablePaginationResource<Entity extends BaseEntity<Id>, Dto extends BaseDto<Entity, Id>, Id> extends BasePaginationResource<Entity, Dto, Id> {
    private Boolean isSupportSelection = null;
    private String entityClassName = null;

    @Operation(summary = "Get Selection data, typically this endpoint used for Dropdown or any other selection data",
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
    @Path("selection")
    public Paginate<SelectionDto> selection(@QueryParam("page") Integer page, @QueryParam("size") Integer size, @Context ContainerRequestContext context) {
        if (isSupportSelection == null) {
            Entity check = getRepository().find("where deletedAt is null").firstResult();
            if (check != null) {
                entityClassName = check.getClass().getName();
                isSupportSelection = check instanceof SelectableEntity;
            } else {
                isSupportSelection = null;
            }
        }

        if (isSupportSelection != null && !isSupportSelection) {
            throw new ForbiddenException("This entity does not support selection, please implement SelectableEntity interface in " + entityClassName + " class");
        }

        if (page == null || page <= 0) {
            page = 1;
        }

        if (size == null || size < 5) {
            size = 50;
        }

        MultivaluedMap<String, String> requestQueries = context.getUriInfo().getQueryParameters();
        return this.getSelection(page, size, requestQueries, context);
    }

    protected Paginate<SelectionDto> getSelection(Integer page, Integer size, MultivaluedMap<String, String> requestQueries, ContainerRequestContext context) {
        Page objPage = Page.of(page - 1, size);
        Sort sort = CrudQueryFilterUtils.fetchSort(context.getUriInfo().getQueryParameters());
        Map<String, Object> queryParams = new HashMap<>();
        String hql = CrudQueryFilterUtils.createFilterQuery(requestQueries, queryParams, this.searchAbleColumn());
        log.debug("generated hql: " + hql);
        log.debug("generated hql value: " + queryParams);
        PanacheQuery<Entity> entityQuery = this.getRepository().find(hql, sort, queryParams);
        long totalCount = entityQuery.count();
        List<Entity> result = entityQuery.page(objPage).list();
        return new Paginate<>(
                result.stream().map(item -> {
                    if (item instanceof SelectableEntity selectable) {
                        return new SelectionDto(selectable.getId(), selectable.getName());
                    }
                    throw new ForbiddenException("Not supported selection endpoint");
                }).toList(),
                objPage.index + 1,
                objPage.size,
                totalCount
        );
    }
}
