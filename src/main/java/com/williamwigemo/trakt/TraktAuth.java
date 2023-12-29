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
    private static final String RedirectUri = "http://localhost:3001/trakt/redirect";

    private String accessToken = null;
    private String code = null;
    private CountDownLatch codeLatch;

    private final TraktApi traktApi;

    public TraktAuth(TraktApi traktApi) {
        this.traktApi = traktApi;
        this.codeLatch = new CountDownLatch(1);
    }

    public String getAuthLink() {
        return "https://trakt.tv/oauth/authorize?client_id=" + this.traktApi.getAppCredentials().traktClientId
                + "&redirect_uri=http%3A%2F%2Flocalhost%3A3001%2Ftrakt%2Fredirect&response_type=code";
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        if (this.accessToken != null) {
            return this.accessToken;
        }

        String accessToken = this.accessToken = TraktAccessTokenManager.getAccessToken();

        long unixTime = System.currentTimeMillis() / 1000L;

        if (unixTime < TraktAccessTokenManager.getCreatedAt() + TraktAccessTokenManager.getExpiresIn()) {
            this.accessToken = accessToken;
        } else if (accessToken != null) {
            // TODO: refresh token
        }

        return this.accessToken;
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
        parameters.put("client_id", this.traktApi.getAppCredentials().traktClientId);
        parameters.put("client_secret", this.traktApi.getAppCredentials().traktClientSecret);
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
            TraktOAuthTokenResponse oauthRes = UrlUtils.parseResponseBody(responseBody, TraktOAuthTokenResponse.class);

            TraktAccessTokenManager.saveOAuthToken(oauthRes);

            return oauthRes.accessToken;
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

    public boolean isAuthenticated() {
        return this.getAccessToken() != null;
    }
}
