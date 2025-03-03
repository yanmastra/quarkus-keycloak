package io.onebyone.quarkus.microservices.common.utils;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.panache.common.Sort;
import io.vertx.ext.web.handler.sockjs.impl.StringEscapeUtils;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class CrudQueryFilterUtils {

    private static final Logger log = Logger.getLogger(CrudQueryFilterUtils.class);
    private static final Set<String> EXCLUDED_PARAMS = Set.of("page", "size", "sort", "count", "order", "index", "sum");

    public static String getFilterQuery(ContainerRequestContext requestContext, Map<String, Object> queryParams, Set<String> searchableColumns) {
        return getFilterQuery(requestContext, queryParams, searchableColumns, "");
    }

    public static String getFilterQuery(ContainerRequestContext requestContext, Map<String, Object> queryParams, Set<String> searchableColumns, String alias) {
        MultivaluedMap<String, String> parameters = requestContext.getUriInfo().getQueryParameters();
        return createFilterQuery(parameters, queryParams, searchableColumns, alias);
    }

    public static String getQueryWhereClause(ContainerRequestContext requestContext, Map<String, Object> queryParams, String alias){
        return getQueryWhereClause(requestContext.getUriInfo().getQueryParameters(), queryParams, alias);
    }

    public static String getQueryWhereClause(ContainerRequestContext requestContext, Map<String, Object> queryParams){
        return getQueryWhereClause(requestContext.getUriInfo().getQueryParameters(), queryParams);
    }

    public static String getQueryWhereClause(MultivaluedMap<String, String> otherQueries, Map<String, Object> queryParams) {
        return getQueryWhereClause(otherQueries, queryParams, "");
    }

    public static String getQueryWhereClause(MultivaluedMap<String, String> otherQueries, Map<String, Object> queryParams, String alias) {
        otherQueries = fetchRequestParams(otherQueries);
        Set<String> whereClauses = otherQueries.entrySet().stream()
                .filter(stringListEntry -> !EXCLUDED_PARAMS.contains(stringListEntry.getKey().toLowerCase()))
                .map(entry -> {
                    ParamToQuery paramToQuery = ParamToQueryFactory.find(entry.getKey(), entry.getValue());
                    queryParams.putAll(paramToQuery.getFieldAndParams(entry.getKey(), entry.getValue(), alias));
                    return paramToQuery.getWhereClause(entry.getKey(), entry.getValue(), alias);
                })
                .collect(Collectors.toSet());

        if (whereClauses.isEmpty()) return "true";
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

    public static String createFilterQuery(MultivaluedMap<String, String> requestParams, Map<String, Object> sqlParams, Set<String> searchableColumn) {
        return createFilterQuery(requestParams, sqlParams, searchableColumn, "");
    }

    public static String createFilterQuery(MultivaluedMap<String, String> requestParams, Map<String, Object> sqlParams, Set<String> searchableColumn, String alias) {
        requestParams = new MultivaluedHashMap<>(requestParams);
        requestParams.remove("page");
        requestParams.remove("size");

        String keyword = "";
        if (requestParams.containsKey("keyword"))
            keyword = requestParams.remove("keyword").getFirst();

        String where = "where "+alias+"deletedAt is null";
        StringBuilder sbQuery = new StringBuilder(where);
        if (StringUtils.isNotBlank(keyword)) {
            sqlParams.put("keyword", "%"+keyword.toLowerCase()+"%");
            Set<String> searchKey = new HashSet<>();
            sbQuery.append(" and (");
            for (String column: searchableColumn) {
                searchKey.add("lower(cast(" + alias + column + " as string)) like :keyword");
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

    private static MultivaluedMap<String, String> fetchRequestParams(MultivaluedMap<String, String> requestParams) {
        MultivaluedMap<String, String> newRequestParams = new MultivaluedHashMap<>();
        try(InstanceHandle<QueryParamParser> instanceHandle = Arc.container().instance(QueryParamParser.class)) {
            QueryParamParser parser = instanceHandle.orElse(null);
            if (parser != null) {
                for (String key : requestParams.keySet()) {
                    if (EXCLUDED_PARAMS.contains(key.toLowerCase())) {
                        newRequestParams.put(key, requestParams.get(key));
                        continue;
                    }

                    List<String> value = requestParams.get(key);
                    log.debug("found key:" + key + ", value: " + value);

                    List<String> parsed = parser.parse(value);
                    if (parsed != null) {
                        log.debug("parsed key:" + key + ", value: " + parsed);
                        newRequestParams.put(key, parsed);
                    }
                }
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return newRequestParams;
    }

    public static Sort fetchSort(MultivaluedMap<String, String> requestParams) {
        List<String> sortParams = requestParams.get("sort");
        if (sortParams != null && !sortParams.isEmpty()) {
            List<String> keys = new ArrayList<>();
            Map<String, String> sortMap = new HashMap<>();

            try(InstanceHandle<QueryParamParser> instanceHandle = Arc.container().instance(QueryParamParser.class)) {
                QueryParamParser parser = instanceHandle.orElse(null);
                if (parser != null) {
                    List<String> parsed = parser.parse(sortParams);
                    if (parsed != null) {
                        Iterator<String> iterator = parsed.iterator();
                        String prevKey = null;
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if (StringUtils.isNotBlank (prevKey) && ("asc".equalsIgnoreCase(key) || "desc".equalsIgnoreCase(key))) {
                                sortMap.put(prevKey, key.toLowerCase());
                            } else {
                                sortMap.put(key, "asc");
                                keys.add(key);
                            }
                            prevKey = key;
                        }
                    } else {
                        throw new Exception("Could not parse query param \"sort\": " + sortParams);
                    }
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                throw new BadRequestException("Unable to get sort order parameters due to error: " + e.getMessage(), e);
            }

            if (!sortMap.isEmpty()) {
                Sort sort = null;
                for (String key : keys) {
                    if (sort == null) {
                        sort = Sort.by(key, "asc".equals(sortMap.get(key)) ? Sort.Direction.Ascending : Sort.Direction.Descending, Sort.NullPrecedence.NULLS_LAST);
                    } else {
                        sort = sort.and(key, "asc".equals(sortMap.get(key)) ? Sort.Direction.Ascending : Sort.Direction.Descending, Sort.NullPrecedence.NULLS_LAST);
                    }
                }

                log.debug("keys: "+keys+", sort: "+sortMap);
                return sort;
            }
        }
        Sort result = Sort.descending("createdAt").and("createdAt", Sort.NullPrecedence.NULLS_LAST);
        log.debug("sort used: "+result);
        return result;
    }
}
