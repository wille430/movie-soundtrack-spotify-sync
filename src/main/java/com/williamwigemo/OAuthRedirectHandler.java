package com.williamwigemo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class OAuthRedirectHandler implements HttpHandler {

    private final OAuthHandler<?> oAuthHandler;

    public OAuthRedirectHandler(OAuthHandler<?> oAuthHandler) {
        this.oAuthHandler = oAuthHandler;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        OutputStream out = httpExchange.getResponseBody();

        String bodyRes = """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8" />
                </head>
                <body></body>
                <script type="text/javascript">
                    window.close();
                </script>
                </html>
                """;

        URI uri = httpExchange.getRequestURI();
        String code = UrlUtils.queryToMap(uri.getQuery()).get("code");
        assert code != null;

        httpExchange.sendResponseHeaders(200, bodyRes.length());
        out.write(bodyRes.getBytes());
        httpExchange.close();

        this.oAuthHandler.setCode(code);
    }
}
