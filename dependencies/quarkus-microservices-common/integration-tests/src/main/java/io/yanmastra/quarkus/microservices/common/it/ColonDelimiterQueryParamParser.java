package io.yanmastra.quarkus.microservices.common.it;

import io.quarkus.arc.Unremovable;
import io.yanmastra.quarkus.microservices.common.utils.QueryParamParser;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Unremovable
@ApplicationScoped
public class ColonDelimiterQueryParamParser extends QueryParamParser {

    @Nullable
    @Override
    public List<String> parse(List<String> values) {
        if (values != null && values.size() == 1) {
            String value = values.getFirst();
            if (StringUtils.isNotBlank(value) && value.contains(",")) {

                String[] split = value.split(",");
                values = Arrays.asList(split);
                return values;
            }
            if (StringUtils.isNotBlank(value) && value.contains("[") && value.contains("]")) {
                values = new ArrayList<>();
                String[] split = value.split("]\\[");
                for (String s: split) {
                    if (s.contains("[")) {
                        values.addAll(Arrays.asList(s.split("\\[")));
                    } else if (s.contains("]")) {
                        values.add(s.replace("]", ""));
                    } else {
                        values.add(s);
                    }
                }
                return values;
            }
        }
        return null;
    }
}
