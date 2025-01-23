package io.yanmastra.quarkus.microservices.common.utils;

import jakarta.annotation.Nullable;

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
