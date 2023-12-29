package com.williamwigemo.spotify;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.williamwigemo.JsonBodyHandler;
import com.williamwigemo.UrlUtils;

public class SpotifyAPI {

    private static final String ApiBaseUrl = "https://api.spotify.com/v1";

    private final String clientId = "d3aa37733ede42e8a7b7d491ccc404f2";
    private HttpClient httpClient;
    private String accessToken = null;
    private SpotifyCurrentUser currentUser = null;
    private CountDownLatch accessTokenLatch;
    private final ObjectMapper objectMapper;
    private final SpotifyOauthServer oauthServer;

    public SpotifyAPI() throws IOException {
        httpClient = HttpClient.newHttpClient();
        this.accessTokenLatch = new CountDownLatch(1);
        this.oauthServer = new SpotifyOauthServer(this);
        this.objectMapper = new ObjectMapper();
    }

    private <T> HttpResponse<T> sendRequest(HttpRequest req, BodyHandler<T> bodyHandler)
            throws SpotifyApiException {
        try {
            return this.httpClient.send(req, bodyHandler);
        } catch (IOException | InterruptedException e) {
            throw new SpotifyApiException(e.getMessage());
        }
    }

    private static String getFormString(HashMap<String, String> params) {
        return params.keySet().stream()
                .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        this.accessTokenLatch.countDown();
    }

    public SpotifyCurrentUser getCurrentUser() throws SpotifyApiException {
        if (currentUser == null) {
            URI uri = URI.create(ApiBaseUrl + "/me");

            HttpRequest req = HttpRequest.newBuilder(uri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + this.accessToken)
                    .GET().build();

            HttpResponse<Supplier<SpotifyCurrentUser>> res = this.sendRequest(req,
                    new JsonBodyHandler<>(SpotifyCurrentUser.class));

            this.currentUser = res.body().get();
        }

        return this.currentUser;
    }

    public void authenticate() {
        this.accessTokenLatch = new CountDownLatch(1);
        this.oauthServer.start();

        List<String> scopes = Arrays.asList("playlist-read-private", "user-read-private", "playlist-modify-private");
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("client_id", this.clientId);
        parameters.put("response_type", "token");
        parameters.put("redirect_uri", "http://localhost:3000/spotify/redirect");
        parameters.put("scope", (String.join(" ", scopes)));
        String form = getFormString(parameters);

        URI uri = URI.create("https://accounts.spotify.com/authorize?" + form);

        System.out.println("Authenticate by following this link: " + uri.toString());

        try {
            accessTokenLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        } finally {
            this.oauthServer.stop();
        }
    }

    public Optional<SpotifyTrack> getTrackByName(String trackName) throws SpotifyApiException {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("q", trackName);
        parameters.put("type", "track");
        String form = getFormString(parameters);

        URI uri = URI.create("https://api.spotify.com/v1/search?" + form);
        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.accessToken)
                .GET().build();

        HttpResponse<Supplier<SpotifySearchResponse>> res = this.sendRequest(req,
                new JsonBodyHandler<>(SpotifySearchResponse.class));

        SpotifySearchResponse searchResponse = res.body().get();

        return searchResponse.tracks.trackObjects.stream()
                .filter(o -> o.name.toLowerCase().contains(trackName.toLowerCase())).findFirst();
    }

    public SpotifyPlaylist createPlaylist(String playlistName) throws SpotifyApiException {
        URI uri = URI.create(ApiBaseUrl + "/users/" + getCurrentUser().id + "/playlists");

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("name", playlistName);
        parameters.put("public", "false");
        String jsonData;
        try {
            jsonData = UrlUtils.hashMapToString(parameters);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            jsonData = "{}";
        }

        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.accessToken)
                .POST(BodyPublishers.ofString(jsonData)).build();

        HttpResponse<String> res = this.sendRequest(req,
                BodyHandlers.ofString());

        String responseBody = res.body();
        if (res.statusCode() != 201) {
            throw new SpotifyApiException(res);
        }

        try {
            return UrlUtils.parseResponseBody(responseBody, SpotifyAddPlaylistResponse.class);
        } catch (IOException e) {
            throw new SpotifyApiException("Could not parse content: " + e.getMessage());
        }
    }

    public List<SpotifyPlaylist> getPlaylists() throws SpotifyApiException {
        URI uri = URI.create(ApiBaseUrl + "/me/playlists");

        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.accessToken)
                .GET().build();

        HttpResponse<Supplier<SpotifyGetPlaylistsResponse>> res = this.sendRequest(req,
                new JsonBodyHandler<>(SpotifyGetPlaylistsResponse.class));

        if (res.statusCode() != 200) {
            throw new RuntimeException("Could not fetch user playlists. Status code: " + res.statusCode());
        }

        return res.body().get().items;
    }

    public void addItemsToPlaylist(String playlistId, List<String> spotifyIds) throws SpotifyApiException {
        if (spotifyIds.isEmpty()) {
            return;
        }

        URI uri = URI.create(ApiBaseUrl + "/playlists/" + playlistId + "/tracks");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("uris", spotifyIds);
        String jsonData;
        try {
            jsonData = UrlUtils.hashMapToString(parameters);
        } catch (JsonProcessingException e) {
            throw new SpotifyApiException("Could not stringify json contents: " + e.getMessage());
        }

        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.accessToken)
                .POST(BodyPublishers.ofString(jsonData)).build();

        HttpResponse<String> res = this.sendRequest(req, BodyHandlers.ofString());

        if (res.statusCode() != 201) {
            throw new SpotifyApiException(res);
        }
    }

    public SpotifyGetPlaylistTracksResponse getPlaylistTracksById(String playlistId) throws SpotifyApiException {
        return getPlaylistTracks(ApiBaseUrl + "/playlists/" + playlistId + "/tracks");
    }

    public SpotifyGetPlaylistTracksResponse getPlaylistTracks(String href)
            throws SpotifyApiException {
        URI uri = URI.create(href);

        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.accessToken)
                .GET().build();

        HttpResponse<String> res = this.sendRequest(req, BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new SpotifyApiException(res);
        }

        try {
            return objectMapper.readValue(res.body(), SpotifyGetPlaylistTracksResponse.class);
        } catch (JsonProcessingException e) {
            throw new SpotifyApiException("Could not parse json response: " + e.getMessage());
        }
    }
}
