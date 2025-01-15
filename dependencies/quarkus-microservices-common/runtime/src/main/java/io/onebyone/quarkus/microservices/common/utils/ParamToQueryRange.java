package io.onebyone.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "range")
public class ParamToQueryRange extends ParamToQuery{

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        String sKey = getSKey(key);
        return "( " + alias + key + " between :"+sKey+"_start and :"+sKey+"_end )";
    }

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        String sKey = getSKey(key);
        return Map.of(
                sKey + "_start", value.get(1),
                sKey + "_end", value.get(2)
        );
    }
}
