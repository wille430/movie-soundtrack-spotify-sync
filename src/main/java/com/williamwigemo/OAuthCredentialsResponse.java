package com.williamwigemo;

public interface OAuthCredentialsResponse {
    public String getAccessToken();

    public int getExpiresIn();

    public String getRefreshToken();
}
