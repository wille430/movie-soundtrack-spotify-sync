package com.williamwigemo.trakt;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.williamwigemo.UrlUtils;

public class TraktAuth {
    public static final String ClientId = "b1aa2d6ad82198a082a961c11bd2dec11a261563b3058c7e93b7296290d0069a";
    private static final String ClientSecret = "8e8c9639dea89bc35aeace933ca03f54efd33773c08c1d6a0f4f084a5afba028";
    private static final String RedirectUri = "http://localhost:3001/trakt/redirect";

    private String accessToken = null;
    private String code = null;
    private CountDownLatch codeLatch;

    private final TraktApi traktApi;

    public TraktAuth(TraktApi traktApi) {
        this.traktApi = traktApi;
        this.codeLatch = new CountDownLatch(1);
    }

    public static String getAuthLink() {
        return "https://trakt.tv/oauth/authorize?client_id=" + ClientId
                + "&redirect_uri=http%3A%2F%2Flocalhost%3A3001%2Ftrakt%2Fredirect&response_type=code";
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        this.codeLatch.countDown();
    }

    public String getAccessTokenFromCode(String code) throws TraktApiException {
        URI uri = URI.create(TraktApi.ApiBaseUrl + "/oauth/token");

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("code", code);
        parameters.put("client_id", ClientId);
        parameters.put("client_secret", ClientSecret);
        parameters.put("redirect_uri", RedirectUri);
        parameters.put("grant_type", "authorization_code");
        String jsonData;
        try {
            jsonData = UrlUtils.hashMapToString(parameters);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            jsonData = "{}";
        }

        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(jsonData)).build();

        HttpResponse<String> res = traktApi.sendRequest(req,
                BodyHandlers.ofString());

        String responseBody = res.body();
        if (res.statusCode() != 200) {
            throw new TraktApiException("" + res.statusCode());
        }

        try {
            return UrlUtils.parseResponseBody(responseBody, TraktOAuthTokenResponse.class).accessToken;
        } catch (IOException e) {
            throw new TraktApiException("Could not parse content: " + e.getMessage());
        }
    }

    public void fetchAccessToken() throws InterruptedException, TraktApiException {
        this.codeLatch.await();
        this.accessToken = this.getAccessTokenFromCode(this.code);
    }

    public void setCodeLatch(CountDownLatch codeLatch) {
        this.codeLatch = codeLatch;
    }
}
