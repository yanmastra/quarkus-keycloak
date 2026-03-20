package io.yanmastra.authentication.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class RefreshTokenPayload {
    @JsonProperty("refresh_token")
    public String refreshToken;
}
