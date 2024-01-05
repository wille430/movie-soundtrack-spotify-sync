package com.williamwigemo.spotify;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpHandler;
import com.williamwigemo.OAuthContexts;
import com.williamwigemo.OAuthRedirectHandler;

public class SpotifyOAuthContexts implements OAuthContexts {
    private final SpotifyAuth spotifyAuth;
    private Map<String, HttpHandler> contexts;

    public SpotifyOAuthContexts(SpotifyAuth spotifyAuth) throws IOException {
        this.spotifyAuth = spotifyAuth;
    }

    @Override
    public Map<String, HttpHandler> getContexts() {
        if (this.contexts == null) {
            this.contexts = new HashMap<>();
            this.contexts.put("/spotify/redirect", new OAuthRedirectHandler(spotifyAuth));
        }

        return this.contexts;
    }
}
