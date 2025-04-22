package io.onebyone.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "notEquals")
public class ParamToQueryNotEquals extends ParamToQuery{

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        return Map.of(getSKey(key), getRealValue(value.get(1)));
    }

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        return alias + key + " != :"+getSKey(key);
    }
}
