package io.yanmastra.commonClass.utils;

import java.util.List;
import java.util.Map;

public class ParamToQueryLessThan extends ParamToQuery{
    ParamToQueryLessThan(String key, List<String> value) {
        super(key, value);
        this.key = key;
        this.sKey = key.replace(".", "_");
        this.value = value.get(1);
    }

    private final String key;
    private final String sKey;
    private final String value;

    @Override
    public String getWhereClause() {
        return key + " <= :"+sKey;
    }

    @Override
    public void attachValue(Map<String, Object> hibernateQueryParams) {
        hibernateQueryParams.put(sKey, value);
    }
}
