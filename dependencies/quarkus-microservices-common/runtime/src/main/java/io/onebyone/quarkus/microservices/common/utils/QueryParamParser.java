package io.onebyone.quarkus.microservices.common.utils;

import io.onebyone.authentication.utils.DateTimeUtils;
import jakarta.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface QueryParamParser {

    /**
     * Use this to parse customized format of query param value
     * @param values parsed values
     * @return return null if the format isn't match or there is any error
     */
    @Nullable
    List<String> parse(List<String> values);
}
