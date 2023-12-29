package com.williamwigemo.spotify;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.williamwigemo.OAuthServer;

public class SpotifyOauthServer implements OAuthServer {
    private final HttpServer server;
    private final SpotifyAuth spotifyAuth;

    public SpotifyOauthServer(SpotifyAuth spotifyAuth) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("localhost", 3000), 0);
        this.spotifyAuth = spotifyAuth;
    }

    public void start() {
        server.createContext("/spotify/redirect", new SpotifyRedirectHandler());
        server.createContext("/authenticate", new SpotifyAuthenticateHandler(spotifyAuth));
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}
