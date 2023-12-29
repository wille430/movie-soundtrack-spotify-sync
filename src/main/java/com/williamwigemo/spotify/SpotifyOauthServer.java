package com.williamwigemo.spotify;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.williamwigemo.OAuthServer;

public class SpotifyOauthServer implements OAuthServer {
    private final HttpServer server;
    private final SpotifyAPI spotifyAPI;

    public SpotifyOauthServer(SpotifyAPI spotifyAPI) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("localhost", 3000), 0);
        this.spotifyAPI = spotifyAPI;
    }

    public void start() {
        server.createContext("/spotify/redirect", new SpotifyRedirectHandler());
        server.createContext("/authenticate", new SpotifyAuthenticateHandler(spotifyAPI));
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}
