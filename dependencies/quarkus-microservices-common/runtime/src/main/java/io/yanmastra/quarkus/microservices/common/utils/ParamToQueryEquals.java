package io.yanmastra.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "equals")
public class ParamToQueryEquals extends ParamToQuery {

    public ParamToQueryEquals() {
    }

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        return "cast(" + alias + key + " as string)=:" + getSKey(key);
    }

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        return Map.of(getSKey(key), value.get(0));
    }
}
