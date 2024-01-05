package com.williamwigemo.trakt;

import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpHandler;
import com.williamwigemo.OAuthContexts;
import com.williamwigemo.OAuthRedirectHandler;

public class TraktOAuthContexts implements OAuthContexts {
    private final TraktAuth traktAuth;

    public TraktOAuthContexts(TraktAuth traktAuth) {
        this.traktAuth = traktAuth;
    }

    private Map<String, HttpHandler> httpContexts;

    private Map<String, HttpHandler> createHttpContexts() {
        Map<String, HttpHandler> map = new HashMap<>();

        map.put("/trakt/redirect", new OAuthRedirectHandler(this.traktAuth));

        return map;
    }

    @Override
    public Map<String, HttpHandler> getContexts() {
        if (this.httpContexts == null) {
            this.httpContexts = createHttpContexts();
        }
        return this.httpContexts;
    }

}
