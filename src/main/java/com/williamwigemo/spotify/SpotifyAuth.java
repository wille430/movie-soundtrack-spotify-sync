package com.williamwigemo.spotify;

import java.util.concurrent.CountDownLatch;

public class SpotifyAuth {

    private String accessToken = null;
    private CountDownLatch accessTokenLatch;

    public SpotifyAuth() {
        this.accessTokenLatch = new CountDownLatch(1);
    }

    public void setAccessTokenLatch(CountDownLatch accessTokenLatch) {
        this.accessTokenLatch = accessTokenLatch;
    }

    public String getAccessToken() {
        if (this.accessToken == null) {
            this.accessToken = SpotifyAccessTokenManager.getAccessToken();
        }

        if (SpotifyAccessTokenManager.hasValidAccessToken()) {
            return this.accessToken;
        }

        return this.accessToken;
    }

    public void setAccessToken(SpotifyTokenResponse res) {
        SpotifyAccessTokenManager.saveOAuthToken(res);
        this.accessToken = res.accessToken;
        this.accessTokenLatch.countDown();
    }

    public boolean isAuthenticated() {
        return this.getAccessToken() != null && SpotifyAccessTokenManager.hasValidAccessToken();
    }

    public CountDownLatch getAccessTokenLatch() {
        return this.accessTokenLatch;
    }
}
