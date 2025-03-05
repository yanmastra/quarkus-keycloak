package io.onebyone.quarkus.microservices.common.crud;

import io.onebyone.quarkus.microservices.common.dto.BaseDto;
import io.onebyone.quarkus.microservices.common.entity.BaseEntity;
import io.onebyone.quarkus.microservices.common.repository.BaseRepository;
import io.onebyone.quarkus.microservices.common.utils.CrudQueryFilterUtils;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.*;

/**
 * You can extend this abstract class to your Resource class to implement Pagination Process (Create Read Update and Delete) activity with non-reactive approach,
 * you should provide 2 generic class parameter
 * @param <Entity> is entity class that extend CrudableEntity
 * @param <Dto> is a Data Access Object class like json representation of the Entity class, you can use your entity class itself if it doesn't have any DAO class
 */
public abstract class BasePaginationResource<Entity extends BaseEntity, Dto extends BaseDto<Entity>> extends io.onebyone.quarkus.microservices.common.v2.crud.BasePaginationResource<Entity, Dto, String> {
    @Override
    protected abstract BaseRepository<Entity, String> getRepository();
    @Override
    protected String toId(String id) {
        return id;
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
