package com.williamwigemo.trakt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TraktOAuthTokenResponse {
    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("token_type")
    public String tokenType;

    @JsonProperty("expires_in")
    public long expiresIn;

    @JsonProperty("refresh_token")
    public String refreshToken;

    @JsonProperty("scope")
    public String scope;

    @JsonProperty("created_at")
    public long createdAt;
}
