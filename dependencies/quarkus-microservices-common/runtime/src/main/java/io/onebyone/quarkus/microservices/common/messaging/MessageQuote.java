package io.onebyone.quarkus.microservices.common.messaging;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import io.smallrye.common.constraint.NotNull;
import io.onebyone.authorization.security.UserPrincipal;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.onebyone.authorization.utils.JsonUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({"data"})
public class MessageQuote {
    @JsonProperty("data")
    private final String data;

    @JsonProperty("principal")
    private final UserPrincipal principal;

    public MessageQuote(@NotNull Object data, @NotNull UserPrincipal principal) {
        this.principal = principal;
        this.data = JsonUtils.toJson(data, true);
    }

    public <T> T getDataObject(Class<T> tClass) {
        return JsonUtils.fromJson(data, tClass, true);
    }

    public <T> T getDataObject(TypeReference<T> tClass) {
        return JsonUtils.fromJson(data, tClass, true);
    }

    @JsonIgnore
    public String getDataJson() {
        return data;
    }

    @NotNull
    @JsonIgnore
    public UserPrincipal getPrincipal() {
        return principal;
    }
}
