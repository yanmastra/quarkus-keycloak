package io.yanmastra.authentication.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshTokenPayload {
    @JsonProperty("refresh_token")
    public String refreshToken;
}
