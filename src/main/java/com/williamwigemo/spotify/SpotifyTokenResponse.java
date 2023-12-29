package com.williamwigemo.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpotifyTokenResponse {
    public String accessToken;
    public String tokenType;
    public int expiresIn;

    public SpotifyTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") int expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }
}
