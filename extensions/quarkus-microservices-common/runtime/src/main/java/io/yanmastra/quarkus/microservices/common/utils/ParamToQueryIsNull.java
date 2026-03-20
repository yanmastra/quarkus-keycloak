package io.yanmastra.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "isNull")
public final class ParamToQueryIsNull extends ParamToQuery {

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        return Map.of();
    }

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        return alias + key + " IS NULL";
    }
}
