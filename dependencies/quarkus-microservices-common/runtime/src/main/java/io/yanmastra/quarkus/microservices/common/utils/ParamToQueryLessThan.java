package io.yanmastra.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "lessThan")
public class ParamToQueryLessThan extends ParamToQuery{

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        return alias + key + " < :"+getSKey(key);
    }

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        return Map.of(getSKey(key), getRealValue(value.get(1)));
    }
}
