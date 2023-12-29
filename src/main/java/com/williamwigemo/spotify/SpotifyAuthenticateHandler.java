package com.williamwigemo.spotify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.williamwigemo.UrlUtils;

public class SpotifyAuthenticateHandler implements HttpHandler {
    private SpotifyAuth spotifyAuth;
    private final ObjectMapper objectMapper;

    public SpotifyAuthenticateHandler(SpotifyAuth spotifyAuth) {
        this.spotifyAuth = spotifyAuth;
        this.objectMapper = new ObjectMapper();
    }

    private static class RequestData {
        @JsonProperty("fragment")
        String fragment;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        OutputStream out = httpExchange.getResponseBody();

        String bodyRes = "";

        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody());
        BufferedReader br = new BufferedReader(isr);

        String requestBody = br.lines().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();

        RequestData data = objectMapper.readValue(requestBody, RequestData.class);
        SpotifyTokenResponse tokenRes = objectMapper.readValue(
                UrlUtils.hashMapToString(UrlUtils.queryToMap(data.fragment)),
                SpotifyTokenResponse.class);

        assert tokenRes.accessToken != null;
        this.spotifyAuth.setAccessToken(tokenRes);

        httpExchange.sendResponseHeaders(200, bodyRes.length());
        out.write(bodyRes.getBytes());
        httpExchange.close();
    }
}