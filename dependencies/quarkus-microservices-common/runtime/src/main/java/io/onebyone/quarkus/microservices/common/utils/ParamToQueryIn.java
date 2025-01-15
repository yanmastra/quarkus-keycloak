package io.onebyone.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "in")
public class ParamToQueryIn extends ParamToQuery{

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        return alias + key + " in (:"+getSKey(key)+")";
    }

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        Set<String> values = new HashSet<>();
        for (int i = 1; i < value.size(); i++) {
            values.add(value.get(i));
        }
        return Map.of(getSKey(key), values);
    }
}
