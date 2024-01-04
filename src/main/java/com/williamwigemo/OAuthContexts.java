package com.williamwigemo;

import java.util.Map;

import com.sun.net.httpserver.HttpHandler;

public interface OAuthContexts {
    public Map<String, HttpHandler> getContexts();
}
