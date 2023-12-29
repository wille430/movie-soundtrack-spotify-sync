package com.williamwigemo.trakt;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.williamwigemo.OAuthServer;

public class TraktOAuthServer implements OAuthServer {
    private final HttpServer server;
    private final TraktAuth traktAuth;

    public TraktOAuthServer(TraktAuth traktAuth) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("localhost", 3001), 0);
        this.traktAuth = traktAuth;
    }

    public void start() {
        server.createContext("/trakt/redirect", new TraktRedirectHandler(this.traktAuth));
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}
