package com.williamwigemo.trakt;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.williamwigemo.AppCredentials;
import com.williamwigemo.UrlUtils;

public class TraktApi {

    public static final String ApiBaseUrl = "https://api.trakt.tv";
    private final HttpClient httpClient;
    private final TraktAuth traktAuth;
    private final TraktOAuthServer oauthServer;
    private final AppCredentials appCredentials;

    public TraktApi(AppCredentials appCredentials) throws IOException {
        this.httpClient = HttpClient.newHttpClient();
        this.traktAuth = new TraktAuth(this);
        this.oauthServer = new TraktOAuthServer(this.traktAuth);
        this.appCredentials = appCredentials;
    }

    public AppCredentials getAppCredentials() {
        return appCredentials;
    }

    public <T> HttpResponse<T> sendRequest(HttpRequest req, BodyHandler<T> bodyHandler)
            throws TraktApiException {
        try {
            return this.httpClient.send(req, bodyHandler);
        } catch (IOException | InterruptedException e) {
            throw new TraktApiException(e.getMessage());
        }
    }

    public void authenticate() throws TraktApiException {
        this.traktAuth.setCodeLatch(new CountDownLatch(1));
        this.oauthServer.start();

        if (!this.traktAuth.isAuthenticated()) {
            System.out.println("Authenticate to Trak.tv by following this link: " + this.traktAuth.getAuthLink());

            try {
                this.traktAuth.fetchAccessToken();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }

        this.oauthServer.stop();
    }

    public List<TraktMovie> getMovieHistory() throws TraktApiException {
        URI uri = URI.create(ApiBaseUrl + "/sync/watched/movies");

        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.traktAuth.getAccessToken())
                .header("trakt-api-key", this.appCredentials.getTraktClientId())
                .GET().build();

        HttpResponse<String> res = this.sendRequest(req,
                BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new TraktApiException(res);
        }

        try {
            return Arrays.asList(UrlUtils.parseResponseBody(res.body(), TraktMovieHistoryResult[].class)).stream()
                    .map(o -> o.movie).toList();
        } catch (IOException e) {
            throw new TraktApiException("Could not parse movie history: " + e.getMessage());
        }
    }
}
