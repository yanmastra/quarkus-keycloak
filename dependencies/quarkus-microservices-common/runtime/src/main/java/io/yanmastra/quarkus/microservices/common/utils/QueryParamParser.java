package io.yanmastra.quarkus.microservices.common.utils;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class QueryParamParser {

    /**
     * Use this to parse customized format of query param value
     * @param values parsed values
     * @return return null if the format isn't match or there is any error
     */
    @Nullable
    public List<String> parse(List<String> values) {
        if (values != null && values.size() == 1) {
            String value = values.getFirst();
            if (StringUtils.isNotBlank(value) && value.contains("|")) {

                String[] split = value.split("\\|");
                values = Arrays.asList(split);
                return values;
            }
        }
        return null;
    }
}
