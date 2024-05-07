package io.yanmastra.commonClass.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CrudQueryFilterUtils {

    @Inject
    Logger log;

    @Inject
    ContainerRequestContext requestContext;

    public String getFilterQuery(Map<String, Object> queryParams, Set<String> searchableColumns) {
        return getFilterQuery(queryParams, searchableColumns, "");
    }

    public String getFilterQuery(Map<String, Object> queryParams, Set<String> searchableColumns, String alias) {
        MultivaluedMap<String, String> parameters = requestContext.getUriInfo().getQueryParameters();
        return createFilterQuery(parameters, queryParams, searchableColumns, alias);
    }

    public String getQueryWhereClause(Map<String, Object> queryParams, String alias){
        return getQueryWhereClause(requestContext.getUriInfo().getQueryParameters(), queryParams, alias);
    }

    public String getQueryWhereClause(Map<String, Object> queryParams){
        return getQueryWhereClause(requestContext.getUriInfo().getQueryParameters(), queryParams);
    }
    public String getQueryWhereClause(MultivaluedMap<String, String> otherQueries, Map<String, Object> queryParams) {
        return getQueryWhereClause(otherQueries, queryParams, "");
    }
    public String getQueryWhereClause(MultivaluedMap<String, String> otherQueries, Map<String, Object> queryParams, String alias) {
        Set<String> whereClauses = otherQueries.entrySet().stream()
                .filter(stringListEntry -> !Set.of("page", "size", "keyword").contains(stringListEntry.getKey()))
                .map(entry -> {
                    ParamToQuery query = ParamToQuery.factory(entry.getKey(), entry.getValue(), alias);
                    log.info("query created:"+query);
                    return query;
                })
                .filter(Objects::nonNull)
                .map(item -> {
                    item.attachValue(queryParams);
                   return item.getWhereClause();
                })
                .collect(Collectors.toSet());

        return String.join(" and ", whereClauses);
    }

    @Deprecated
    public String createFilterQuery(String keyword, Map<String, String> otherQueries, Map<String, Object> queryParams, Set<String> searchableColumn) {
        String where = "where deletedAt is null";
        StringBuilder sbQuery = new StringBuilder(where);
        if (StringUtils.isNotBlank(keyword)) {
            queryParams.put("keyword", "%"+keyword+"%");
            Set<String> searchKey = new HashSet<>();
            sbQuery.append(" and (");
            for (String column: searchableColumn) {
                searchKey.add("cast(" + column + " as string) like :keyword");
            }
            sbQuery.append(String.join(" or ", searchKey)).append(")");
        }

        if (!otherQueries.isEmpty()) {
            sbQuery.append(" and ");
        }

        if (!otherQueries.isEmpty()) {
            Iterator<String> keyQueries = otherQueries.keySet().iterator();
            while (keyQueries.hasNext()) {
                String key = keyQueries.next();
                String sKey = key.replace(".", "_");
                queryParams.put(sKey, otherQueries.get(key));

                sbQuery.append(key).append("=:").append(sKey);
                if (keyQueries.hasNext()) sbQuery.append(" and ");
            }
        }
        return sbQuery.toString();
    }

    public String createFilterQuery(MultivaluedMap<String, String> requestParams, Map<String, Object> sqlParams, Set<String> searchableColumn) {
        return createFilterQuery(requestParams, sqlParams, searchableColumn, "");
    }

    public String createFilterQuery(MultivaluedMap<String, String> requestParams, Map<String, Object> sqlParams, Set<String> searchableColumn, String alias) {
        requestParams = new MultivaluedHashMap<>(requestParams);
        requestParams.remove("page");
        requestParams.remove("size");

        String keyword = "";
        if (requestParams.containsKey("keyword"))
            keyword = requestParams.remove("keyword").getFirst();

        String where = "where "+alias+"deletedAt is null";
        StringBuilder sbQuery = new StringBuilder(where);
        if (StringUtils.isNotBlank(keyword)) {
            sqlParams.put("keyword", "%"+keyword+"%");
            Set<String> searchKey = new HashSet<>();
            sbQuery.append(" and (");
            for (String column: searchableColumn) {
                searchKey.add("cast(" + alias + column + " as string) like :keyword");
            }
            sbQuery.append(String.join(" or ", searchKey)).append(")");
        }

        if (!requestParams.isEmpty()) {
            sbQuery.append(" and ");
        }

        if (!requestParams.isEmpty()) {
            sbQuery.append(getQueryWhereClause(requestParams, sqlParams));
        }
        return sbQuery.toString();
    }
}
