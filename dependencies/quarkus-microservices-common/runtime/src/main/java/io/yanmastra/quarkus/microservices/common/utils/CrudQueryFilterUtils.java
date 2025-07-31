package io.yanmastra.quarkus.microservices.common.utils;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class CrudQueryFilterUtils {

    private static final Logger log = Logger.getLogger(CrudQueryFilterUtils.class);
    public static final String PAGE = "page";
    public static final String SIZE = "size";
    public static final String SORT = "sort";
    public static final String COUNT = "count";
    public static final String ORDER = "order";
    public static final String INDEX = "index";
    public static final String SUM = "sum";
    public static final String KEYWORD = "keyword";
    private static final Set<String> EXCLUDED_PARAMS = Set.of(PAGE, SIZE, SORT, COUNT, ORDER, INDEX, SUM);


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
                .filter(stringListEntry -> {
                    for (String param : stringListEntry.getValue()) {
                        if (StringUtils.isBlank(param)) return false;
                    }
                    return true;
                })
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
            queryParams.put(KEYWORD, "%"+keyword+"%");
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
        requestParams.remove(PAGE);
        requestParams.remove(SIZE);

        String keyword = null;
        if (requestParams.containsKey(KEYWORD)) {
            List<String> keywordValues = requestParams.remove(KEYWORD);
            if (!keywordValues.isEmpty()) {
                keyword = keywordValues.getFirst();
            }
        }

        String where = "where "+alias+"deletedAt is null";
        StringBuilder sbQuery = new StringBuilder(where);
        if (StringUtils.isNotBlank(keyword)) {
            sqlParams.put(KEYWORD, "%"+keyword.toLowerCase()+"%");
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
        return fetchRequestParams(requestParams, null);
    }

    private static MultivaluedMap<String, String> fetchRequestParams(MultivaluedMap<String, String> requestParams, String specificKey) {
        MultivaluedMap<String, String> newRequestParams = new MultivaluedHashMap<>();
        requestParams.keySet().forEach(key -> {
            List<String> values = requestParams.get(key);
            if (values != null && !values.isEmpty()) {
                values = values.stream().filter(StringUtils::isNotBlank).toList();
            }

            if (values != null && !values.isEmpty()) {
                if (ParamToQueryRange.KEY.equals(values.getFirst()) && values.size() != 3) return;

                newRequestParams.put(key, values);
            }
        });

        try(InstanceHandle<QueryParamParser> instanceHandle = Arc.container().instance(QueryParamParser.class)) {
            QueryParamParser parser = instanceHandle.orElse(null);
            if (parser != null) {
                if (StringUtils.isNotBlank(specificKey) && requestParams.containsKey(specificKey)) {
                    List<String> value = requestParams.get(specificKey);
                    List<String> parsed = parser.parse(value);
                    if (parsed != null) {
                        log.debug("parsed key:" + specificKey + ", value: " + parsed);
                        newRequestParams.put(specificKey, parsed);
                    }
                } else {
                    for (String key : requestParams.keySet()) {
                        List<String> value = requestParams.get(key);
                        log.debug("found key:" + key + ", value: " + value);

                        List<String> parsed = parser.parse(value);
                        if (parsed != null) {
                            log.debug("parsed key:" + key + ", value: " + parsed);
                            newRequestParams.put(key, parsed);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return newRequestParams;
    }

    public static Sort fetchSort(MultivaluedMap<String, String> requestParams) {
        requestParams = fetchRequestParams(requestParams, "sort");

        List<String> sortParams = requestParams.get("sort");
        if (sortParams != null && !sortParams.isEmpty()) {
            List<String> keys = new ArrayList<>();
            Map<String, String> sortMap = new HashMap<>();

            Iterator<String> iterator = sortParams.iterator();
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
