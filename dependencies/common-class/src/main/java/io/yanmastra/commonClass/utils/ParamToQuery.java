package io.yanmastra.commonClass.utils;

import io.vertx.ext.web.handler.HttpException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

abstract class ParamToQuery {
    private static final Logger log = Logger.getLogger(ParamToQuery.class.getName());

    protected String key;
    protected String alias;

    ParamToQuery(String key, List<String> value, String alias){
        this.key = key;
        this.alias = alias;
    }

    abstract String getWhereClause();
    abstract void attachValue(Map<String, Object> hibernateQueryParams);

    static ParamToQuery factory(String key, List<String> value, String alias) {
        if (value == null || value.isEmpty()) return null;

        log.info("creating query:"+key+", "+value.size()+", items:"+value);

        if (value.size() == 1) {
            String first = value.getFirst();
            if (StringUtils.isNotBlank(first) && first.contains(",")) {
                value = Arrays.asList(first.split(","));
            }
        }

        if (value.size() == 1)
            return new ParamToQueryEquals(key, value, alias);
        if (value.size() >= 3) {
            if ("range".equals(value.getFirst())) {
                return new ParamToQueryRange(key, value, alias);
            } else if ("in".equals(value.getFirst())) {
                return new ParamToQueryIn(key, value, alias);
            } else if ("notIn".equals(value.getFirst())) {
                return new ParamToQueryNotIn(key, value, alias);
            }
        } else {
            if ("greaterThan".equals(value.getFirst())) {
                return new ParamToQueryGreaterThan(key, value, alias);
            } else if ("lessThan".equals(value.getFirst())) {
                return new ParamToQueryLessThan(key, value, alias);
            } else if ("notEquals".equals(value.getFirst())) {
                return new ParamToQueryNotEquals(key, value, alias);
            }
        }
        throw new HttpException(HttpStatus.SC_BAD_REQUEST, "Wrong value supplied to query param");
    }
}
