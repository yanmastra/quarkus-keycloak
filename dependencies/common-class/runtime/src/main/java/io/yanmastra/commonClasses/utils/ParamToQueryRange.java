package io.yanmastra.commonClasses.utils;

import java.util.List;
import java.util.Map;

public class ParamToQueryRange extends ParamToQuery{
    private final String sKey;
    private final String valueStart;
    private final String valueEnd;

    public ParamToQueryRange(String key, List<String> value, String alias) {
        super(key, value, alias);
        valueStart = value.get(1);
        valueEnd = value.get(2);
        sKey = key.replace(".", "_");
    }

    @Override
    public String getWhereClause() {
        return "( " + alias + key + " between :"+sKey+"_start and :"+sKey+"_end )";
    }

    @Override
    public void attachValue(Map<String, Object> hibernateQueryParams) {
        hibernateQueryParams.put(sKey + "_start", valueStart);
        hibernateQueryParams.put(sKey + "_end", valueEnd);
    }
}
