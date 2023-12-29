package com.williamwigemo.trakt;

import java.util.prefs.Preferences;

public class TraktAccessTokenManager {
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String EXPIRES_IN_KEY = "expires_in";
    private static final String CREATED_AT_KEY = "create_at";
    private static final String REFRESH_TOKEN_KEY = "refresh_token_key";

    public static void saveOAuthToken(TraktOAuthTokenResponse res) {
        Preferences prefs = Preferences.userNodeForPackage(TraktAccessTokenManager.class);
        prefs.put(ACCESS_TOKEN_KEY, res.accessToken);
        prefs.put(EXPIRES_IN_KEY, "" + res.expiresIn);
        prefs.put(CREATED_AT_KEY, "" + res.createdAt);
        prefs.put(REFRESH_TOKEN_KEY, res.refreshToken);
    }

    public static String getAccessToken() {
        Preferences prefs = Preferences.userNodeForPackage(TraktAccessTokenManager.class);
        return prefs.get(ACCESS_TOKEN_KEY, null);
    }

    public static long getExpiresIn() {
        Preferences prefs = Preferences.userNodeForPackage(TraktAccessTokenManager.class);
        String expiresIN = prefs.get(EXPIRES_IN_KEY, null);
        return expiresIN != null ? Long.parseLong(expiresIN) : -1;
    }

    public static long getCreatedAt() {
        Preferences prefs = Preferences.userNodeForPackage(TraktAccessTokenManager.class);
        String createdAt = prefs.get(CREATED_AT_KEY, null);
        return createdAt != null ? Long.parseLong(createdAt) : -1;
    }
}
