package com.williamwigemo.trakt.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.williamwigemo.OAuthCredentialsResponse;

public class TraktOAuthTokenResponse implements OAuthCredentialsResponse {
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

    @Override
    public String getAccessToken() {
        return this.accessToken;
    }

    @Override
    public int getExpiresIn() {
        return (int) this.expiresIn;
    }

    @Override
    public String getRefreshToken() {
        return this.refreshToken;
    }
}
