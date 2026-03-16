package io.yanmastra.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "notEquals")
public final class ParamToQueryNotEquals extends ParamToQuery{

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        return Map.of(getSKey(key), getRealValue(value.get(1)));
    }

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        Object oValue = getRealValue(value.getFirst());
        String sKey = key;
        if (oValue instanceof String) {
            sKey = "cast(" + alias + key + " as string)";
        } else if (oValue instanceof Date) {
            sKey = "cast(" + alias + key + " as date)";
        }
        return sKey + " != :"+getSKey(key);
    }
}
