package io.yanmastra.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "greaterThan")
public final class ParamToQueryGreaterThan extends ParamToQuery {

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        Object oValue = getRealValue(value.get(1));
        String sKey = alias + key;
        if (oValue instanceof String) {
            throw new IllegalArgumentException("Unable to compare strings by lessThan or greaterThan");
        } else if (oValue instanceof Date) {
            sKey = "cast(" + alias + key + " as date)";
        }
        return sKey + " > :"+getSKey(key);
    }

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        return Map.of(getSKey(key), getRealValue(value.get(1)));
    }
}
