package com.williamwigemo.trakt;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.williamwigemo.AppLogging;
import com.williamwigemo.AppSettings;
import com.williamwigemo.OAuthCredentialsResponse;
import com.williamwigemo.OAuthHandler;
import com.williamwigemo.OAuthHttpServer;
import com.williamwigemo.UrlUtils;
import com.williamwigemo.trakt.dtos.TraktOAuthTokenResponse;

public class TraktAuth extends OAuthHandler<TraktApiException> {
    private static final String RedirectUri = OAuthHttpServer.getBaseUrl() + "/trakt/redirect";

    private final TraktApi traktApi;

    public TraktAuth(TraktApi traktApi) {
        super("Trakt.tv", AppLogging.buildLogger(TraktAuth.class), TraktAuth.class);
        this.traktApi = traktApi;
    }

    @Override
    public String createAuthorizeUrl() {
        return String.format("https://trakt.tv/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                AppSettings.getSettings().getTraktClientId(), RedirectUri);
    }

    @Override
    public OAuthCredentialsResponse fetchAccessTokenFromCode() throws TraktApiException {
        URI uri = URI.create(TraktApi.ApiBaseUrl + "/oauth/token");

        AppSettings creds = AppSettings.getSettings();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("code", this.getCode());
        parameters.put("client_id", creds.traktClientId);
        parameters.put("client_secret", creds.traktClientSecret);
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
            return UrlUtils.parseResponseBody(responseBody, TraktOAuthTokenResponse.class);
        } catch (IOException e) {
            throw new TraktApiException("Could not parse content: " + e.getMessage());
        }
    }
}
