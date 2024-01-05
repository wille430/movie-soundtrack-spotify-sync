package com.williamwigemo.spotify;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import com.williamwigemo.AppLogging;
import com.williamwigemo.AppSettings;
import com.williamwigemo.OAuthCredentialsResponse;
import com.williamwigemo.OAuthHandler;
import com.williamwigemo.OAuthHttpServer;
import com.williamwigemo.UrlUtils;
import com.williamwigemo.spotify.dtos.SpotifyOAuthTokenResponse;

public class SpotifyAuth extends OAuthHandler<SpotifyApiException> {

    private static final String AccountBaseUrl = "https://accounts.spotify.com";
    private static final String RedirectUrl = OAuthHttpServer.getBaseUrl() + "/spotify/redirect";

    private SpotifyAPI spotifyAPI;

    public SpotifyAuth(SpotifyAPI spotifyAPI) throws IOException {
        super("Spotify", AppLogging.buildLogger(SpotifyAuth.class), SpotifyAuth.class);
        this.setOAuthContexts(new SpotifyOAuthContexts(this));
        this.spotifyAPI = spotifyAPI;
    }

    public OAuthCredentialsResponse fetchAccessTokenFromCode() throws SpotifyApiException {
        URI uri = URI.create(AccountBaseUrl + "/api/token");

        AppSettings creds = AppSettings.getSettings();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("code", this.getCode());
        parameters.put("redirect_uri", RedirectUrl);
        parameters.put("grant_type", "authorization_code");
        String formData = UrlUtils.getQueryString(parameters);

        String authStr = "Basic " + Base64
                .getEncoder()
                .withoutPadding()
                .encodeToString(String.format("%s:%s", creds.getSpotifyClientId(),
                        creds.getSpotifyClientSecret()).getBytes());

        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", authStr)
                .POST(BodyPublishers.ofString(formData)).build();

        HttpResponse<String> res = this.spotifyAPI.sendRequest(req,
                BodyHandlers.ofString());

        String responseBody = res.body();
        if (res.statusCode() != 200) {
            throw new SpotifyApiException(res);
        }

        try {
            SpotifyOAuthTokenResponse oauthRes = UrlUtils.parseResponseBody(responseBody,
                    SpotifyOAuthTokenResponse.class);

            return oauthRes;
        } catch (IOException e) {
            throw new SpotifyApiException("Could not parse content: " + e.getMessage());
        }
    }

    public String createAuthorizeUrl() {
        AppSettings settings = AppSettings.getSettings();

        List<String> scopes = Arrays.asList("playlist-read-private", "user-read-private", "playlist-modify-private");
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("client_id", settings.getSpotifyClientId());
        parameters.put("response_type", "code");
        parameters.put("redirect_uri", RedirectUrl);
        parameters.put("scope", (String.join(" ", scopes)));
        String form = UrlUtils.getQueryString(parameters);

        URI uri = URI.create(AccountBaseUrl + "/authorize?" + form);

        return uri.toString();
    }
}
