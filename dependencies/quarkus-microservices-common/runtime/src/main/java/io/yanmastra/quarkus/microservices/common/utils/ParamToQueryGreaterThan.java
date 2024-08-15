package io.yanmastra.quarkus.microservices.common.utils;

import java.util.List;
import java.util.Map;

public class ParamToQueryGreaterThan extends ParamToQuery{
    ParamToQueryGreaterThan(String key, List<String> value, String alias) {
        super(key, value, alias);
        this.sKey = key.replace(".", "_");
        this.value = value.get(1);
    }
    private final String sKey;
    private final String value;

    @Override
    public String getWhereClause() {
        return alias + key + " >= :"+sKey;
    }

    @Override
    public void attachValue(Map<String, Object> hibernateQueryParams) {
        hibernateQueryParams.put(sKey, value);
    }
}
