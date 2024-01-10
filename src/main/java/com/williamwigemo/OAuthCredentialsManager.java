package com.williamwigemo;

public class OAuthCredentialsManager {
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String EXPIRES_IN_KEY = "expires_in";
    private static final String CREATED_AT_KEY = "create_at";
    private static final String REFRESH_TOKEN = "refresh_token";

    private final AppProperties prefs;

    public <T> OAuthCredentialsManager(Class<T> cls) {
        this.prefs = AppProperties.getProperties(cls);
    }

    public void saveOAuthCredentials(OAuthCredentialsResponse obj) {
        prefs.put(ACCESS_TOKEN_KEY, obj.getAccessToken());
        prefs.put(EXPIRES_IN_KEY, "" + obj.getExpiresIn());
        prefs.put(CREATED_AT_KEY, "" + System.currentTimeMillis() / 1000L);

        if (obj.getRefreshToken() != null)
            prefs.put(REFRESH_TOKEN, obj.getRefreshToken());

        prefs.store();
    }

    public String getAccessToken() {
        return prefs.get(ACCESS_TOKEN_KEY, null);
    }

    public long getExpiresIn() {
        String expiresIN = prefs.get(EXPIRES_IN_KEY, null);
        return expiresIN != null ? Long.parseLong(expiresIN) : -1;
    }

    public long getCreatedAt() {
        String createdAt = prefs.get(CREATED_AT_KEY, null);
        return createdAt != null ? Long.parseLong(createdAt) : -1;
    }

    public boolean hasValidAccessToken() {
        long unixTime = System.currentTimeMillis() / 1000l;
        return unixTime < getExpiresIn() + getCreatedAt() && getAccessToken() != null;
    }

    public void clearOAuthCredentials() {
        prefs.clear();
    }

    public String getRefreshToken() {
        return prefs.get(REFRESH_TOKEN, null);
    }
}