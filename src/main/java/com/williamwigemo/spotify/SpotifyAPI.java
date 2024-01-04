package com.williamwigemo.spotify;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.williamwigemo.JsonBodyHandler;
import com.williamwigemo.OAuthHttpServer;
import com.williamwigemo.TrackSimilarity;
import com.williamwigemo.UrlUtils;

public class SpotifyAPI {

    private static final String ApiBaseUrl = "https://api.spotify.com/v1";
    private HttpClient httpClient;
    private SpotifyCurrentUser currentUser = null;
    private final ObjectMapper objectMapper;
    private String clientId;
    private SpotifyAuth spotifyAuth;

    public SpotifyAPI(String clientId) throws IOException {
        this.clientId = clientId;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.spotifyAuth = new SpotifyAuth();
    }

    private <T> HttpResponse<T> sendRequest(HttpRequest req, BodyHandler<T> bodyHandler)
            throws SpotifyApiException {
        try {
            return this.httpClient.send(req, bodyHandler);
        } catch (IOException | InterruptedException e) {
            throw new SpotifyApiException(e.getMessage());
        }
    }

    public SpotifyCurrentUser getCurrentUser() throws SpotifyApiException {
        if (currentUser == null) {
            URI uri = URI.create(ApiBaseUrl + "/me");

            HttpRequest req = HttpRequest.newBuilder(uri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + this.spotifyAuth.getAccessToken())
                    .GET().build();

            HttpResponse<Supplier<SpotifyCurrentUser>> res = this.sendRequest(req,
                    new JsonBodyHandler<>(SpotifyCurrentUser.class));

            this.currentUser = res.body().get();
        }

        return this.currentUser;
    }

    public void authenticate() throws IOException {
        if (this.spotifyAuth.isAuthenticated()) {
            return;
        }

        this.spotifyAuth.setAccessTokenLatch(new CountDownLatch(1));

        OAuthHttpServer httpServer = OAuthHttpServer.getInstance();
        httpServer.start(new SpotifyOAuthContexts(this.spotifyAuth));

        List<String> scopes = Arrays.asList("playlist-read-private", "user-read-private", "playlist-modify-private");
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("client_id", this.clientId);
        parameters.put("response_type", "token");
        parameters.put("redirect_uri", OAuthHttpServer.getBaseUrl() + "/spotify/redirect");
        parameters.put("scope", (String.join(" ", scopes)));
        String form = UrlUtils.getQueryString(parameters);

        URI uri = URI.create("https://accounts.spotify.com/authorize?" + form);

        System.out.println("Authenticate by following this link: " + uri.toString());

        try {
            this.spotifyAuth.getAccessTokenLatch().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        } finally {
            httpServer.stop();
        }
    }

    public Optional<SpotifyTrack> getTrackByName(String trackName) throws SpotifyApiException {
        return getTrackByName(trackName, null);
    }

    public Optional<SpotifyTrack> getTrackByName(String trackName, String artist) throws SpotifyApiException {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "track");

        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append(trackName);
        if (artist != null) {
            queryBuilder.append(" " + "artist:" + artist);
        }

        parameters.put("q", queryBuilder.toString());

        String form = UrlUtils.getQueryString(parameters);

        URI uri = URI.create("https://api.spotify.com/v1/search?" + form);
        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.spotifyAuth.getAccessToken())
                .GET().build();

        HttpResponse<String> res = this.sendRequest(req, BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new SpotifyApiException(res);
        }

        SpotifySearchResponse searchResponse;
        try {
            searchResponse = UrlUtils.parseResponseBody(res.body(), SpotifySearchResponse.class);
        } catch (IOException e) {
            throw new SpotifyApiException("Could not parse contents: " + e.getMessage());
        }

        Stream<SpotifyTrack> stream = searchResponse.tracks.trackObjects.stream()
                .filter(o -> TrackSimilarity.isSameTrack(o, trackName, artist));

        return stream.findFirst();
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
                .header("Authorization", "Bearer " + this.spotifyAuth.getAccessToken())
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
                .header("Authorization", "Bearer " + this.spotifyAuth.getAccessToken())
                .GET().build();

        HttpResponse<Supplier<SpotifyGetPlaylistsResponse>> res = this.sendRequest(req,
                new JsonBodyHandler<>(SpotifyGetPlaylistsResponse.class));

        if (res.statusCode() != 200) {
            throw new RuntimeException("Could not fetch user playlists. Status code: " + res.statusCode());
        }

        return res.body().get().items;
    }

    private void addItemsToPlaylist(String playlistId, List<String> spotifyIds, int start) throws SpotifyApiException {
        if (spotifyIds.isEmpty()) {
            return;
        }

        if (start > spotifyIds.size() - 1) {
            return;
        }

        spotifyIds = spotifyIds.subList(start, Math.min(spotifyIds.size(), start + 100));

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
                .header("Authorization", "Bearer " + this.spotifyAuth.getAccessToken())
                .POST(BodyPublishers.ofString(jsonData)).build();

        HttpResponse<String> res = this.sendRequest(req, BodyHandlers.ofString());

        if (res.statusCode() != 201) {
            throw new SpotifyApiException(res);
        }
    }

    public void addItemsToPlaylist(String playlistId, List<String> spotifyIds) throws SpotifyApiException {
        for (int i = 0; i < spotifyIds.size(); i += 100) {
            addItemsToPlaylist(playlistId, spotifyIds, i);
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
                .header("Authorization", "Bearer " + this.spotifyAuth.getAccessToken())
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
