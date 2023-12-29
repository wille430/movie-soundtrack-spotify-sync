package com.williamwigemo.spotify;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class SpotifyRedirectHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        OutputStream out = httpExchange.getResponseBody();

        httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

        String script = """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8" />
                </head>
                <body></body>
                <script type="text/javascript">
                    var fragment = window.location.hash.substring(1);
                    var xhr = new XMLHttpRequest();
                    xhr.open('POST', '/authenticate');
                    xhr.setRequestHeader("Content-Type", "application/json");
                    xhr.send(JSON.stringify({
                        fragment: fragment
                    }));
                </script>
                </html>
                """;
        httpExchange.sendResponseHeaders(200, script.length());
        out.write(script.getBytes());
        httpExchange.close();
    }
}
