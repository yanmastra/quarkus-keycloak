package io.onebyone.authentication.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshTokenPayload {
    @JsonProperty("refresh_token")
    public String refreshToken;
}
