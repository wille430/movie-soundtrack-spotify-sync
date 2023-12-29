package com.williamwigemo.spotify;

import java.util.prefs.Preferences;

public class SpotifyAccessTokenManager {
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String EXPIRES_IN_KEY = "expires_in";
    private static final String CREATED_AT_KEY = "create_at";

    public static void saveOAuthToken(SpotifyTokenResponse res) {
        Preferences prefs = Preferences.userNodeForPackage(SpotifyAccessTokenManager.class);
        prefs.put(ACCESS_TOKEN_KEY, res.accessToken);
        prefs.put(EXPIRES_IN_KEY, "" + res.expiresIn);
        prefs.put(CREATED_AT_KEY, "" + System.currentTimeMillis() / 1000L);
    }

    public static String getAccessToken() {
        Preferences prefs = Preferences.userNodeForPackage(SpotifyAccessTokenManager.class);
        return prefs.get(ACCESS_TOKEN_KEY, null);
    }

    public static long getExpiresIn() {
        Preferences prefs = Preferences.userNodeForPackage(SpotifyAccessTokenManager.class);
        String expiresIN = prefs.get(EXPIRES_IN_KEY, null);
        return expiresIN != null ? Long.parseLong(expiresIN) : -1;
    }

    public static long getCreatedAt() {
        Preferences prefs = Preferences.userNodeForPackage(SpotifyAccessTokenManager.class);
        String createdAt = prefs.get(CREATED_AT_KEY, null);
        return createdAt != null ? Long.parseLong(createdAt) : -1;
    }

    public static boolean hasValidAccessToken() {
        long unixTime = System.currentTimeMillis() / 1000L;

        return unixTime < getExpiresIn() + getCreatedAt() && getAccessToken() != null;
    }
}
