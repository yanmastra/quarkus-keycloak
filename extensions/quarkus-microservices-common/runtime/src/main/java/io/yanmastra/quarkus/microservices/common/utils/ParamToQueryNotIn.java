package io.yanmastra.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;

import java.util.*;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "notIn")
public final class ParamToQueryNotIn extends ParamToQuery{

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        Set<Object> values = new HashSet<>();
        for (int i = 1; i < value.size(); i++) {
            values.add(getRealValue(value.get(i)));
        }
        return Map.of(getSKey(key), values);
    }

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        Object oValue = getRealValue(value.get(1));
        String sKey = alias + key;
        if (oValue instanceof String) {
            sKey = "cast(" + alias + key + " as string)";
        } else if (oValue instanceof Date) {
            sKey = "cast(" + alias + key + " as date)";
        }
        return sKey + " not in (:"+getSKey(key)+")";
    }
}
