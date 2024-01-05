package com.williamwigemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class OAuthHttpServer {

    private static OAuthHttpServer oAuthHttpServer;
    private HttpServer httpServer;
    private final Logger logger = AppLogging.buildLogger(OAuthHttpServer.class);

    private static final AppSettings Settings = AppSettings.getSettings();
    public static final int Port = Settings.getPort();

    public static OAuthHttpServer getInstance() throws IOException {
        if (oAuthHttpServer == null) {
            oAuthHttpServer = new OAuthHttpServer();
        }

        return oAuthHttpServer;
    }

    private OAuthHttpServer() {
    }

    public void start(OAuthContexts oAuthServer) throws IOException {
        if (this.httpServer != null) {
            throw new RuntimeException("OAuth server is already in use");
        }

        this.httpServer = HttpServer.create(new InetSocketAddress(Port), 0);

        Map<String, HttpHandler> contexts = oAuthServer.getContexts();

        for (String path : contexts.keySet()) {
            logger.fine(String.format("Created endpoint at %s%s", Settings.getAppUrl(), path));
            HttpHandler handler = contexts.get(path);
            this.httpServer.createContext(path, handler);
        }

        logger.fine("Started HTTP server on port " + this.httpServer.getAddress().getPort());

        this.httpServer.setExecutor(null);
        this.httpServer.start();
    }

    public void stop() {
        logger.fine("Stopping HTTP server on port " + this.httpServer.getAddress().getPort());
        this.httpServer.stop(0);
        this.httpServer = null;
    }
}
