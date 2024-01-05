package com.williamwigemo.spotify.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.williamwigemo.OAuthCredentialsResponse;

public class SpotifyOAuthTokenResponse implements OAuthCredentialsResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String token_type;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public String getToken_type() {
        return token_type;
    }

    public String getScope() {
        return scope;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
