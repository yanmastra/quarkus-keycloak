package io.onebyone.quarkus.microservices.common.it.bean;

import io.onebyone.quarkus.microservices.common.utils.QueryParamParser;
import io.quarkus.arc.Unremovable;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

@Unremovable
@Singleton
public class ColonDelimiterQueryParamParser implements QueryParamParser {

    @Nullable
    @Override
    public List<String> parse(List<String> values) {
        if (values != null && values.size() == 1) {
            String value = values.getFirst();
            if (StringUtils.isNotBlank(value) && value.contains(":")) {

                String[] split = value.split(":");
                values = Arrays.asList(split);
                return values;
            }
        }
        return values;
    }
}
