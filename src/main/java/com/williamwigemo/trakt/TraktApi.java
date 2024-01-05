package com.williamwigemo.trakt;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.williamwigemo.AppSettings;
import com.williamwigemo.UrlUtils;
import com.williamwigemo.trakt.dtos.TraktMovie;
import com.williamwigemo.trakt.dtos.TraktMovieHistoryResult;
import com.williamwigemo.trakt.dtos.TraktWatchedResult;
import com.williamwigemo.trakt.dtos.TraktWatchedShowsResult;

public class TraktApi {

    public static final String ApiBaseUrl = "https://api.trakt.tv";
    private final HttpClient httpClient;
    private final TraktAuth traktAuth;
    private final AppSettings appCredentials;
    private final TraktHistoryManager historyManager;

    public TraktApi() throws IOException {
        this.httpClient = HttpClient.newHttpClient();
        this.traktAuth = new TraktAuth(this);
        this.traktAuth.setOAuthContexts(new TraktOAuthContexts(traktAuth));

        this.appCredentials = AppSettings.getSettings();
        this.historyManager = TraktHistoryManager.getInstance();
    }

    public <T> HttpResponse<T> sendRequest(HttpRequest req, BodyHandler<T> bodyHandler)
            throws TraktApiException {
        try {
            return this.httpClient.send(req, bodyHandler);
        } catch (IOException | InterruptedException e) {
            throw new TraktApiException(e.getMessage());
        }
    }

    public TraktAuth getTraktAuth() {
        return traktAuth;
    }

    public void authenticate() throws TraktApiException, IOException {
        this.traktAuth.authorize();
    }

    public List<TraktMovie> syncMovieHistory() throws TraktApiException {
        Date lastMovieSync = this.historyManager.getLastMovieSync();
        List<TraktMovieHistoryResult> movies = getMovieHistory();

        if (lastMovieSync != null) {
            movies = movies.stream()
                    .filter(o -> {
                        try {
                            return UrlUtils.parseISO8601Date(o.getLastUpdatedAt()).compareTo(lastMovieSync) > 0;
                        } catch (ParseException e) {
                            return false;
                        }
                    })
                    .toList();
        }

        return movies.stream().map(o -> o.getMovie()).toList();
    }

    public List<TraktMovieHistoryResult> getMovieHistory() throws TraktApiException {
        return getHistory("movies", TraktMovieHistoryResult[].class);
    }

    public <T extends TraktWatchedResult<?>> List<T> getHistory(String type, Class<T[]> cls)
            throws TraktApiException {
        URI uri = URI.create(ApiBaseUrl + "/sync/watched/" + type);

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
            return Arrays.asList(UrlUtils.parseResponseBody(res.body(), cls));
        } catch (IOException e) {
            throw new TraktApiException("Could not parse history: " + e.getMessage());
        }

    }

    public List<TraktWatchedShowsResult> getShowsHistory() throws TraktApiException {
        return getHistory("shows", TraktWatchedShowsResult[].class);
    }

    public List<TraktMovie> getWatchedMovies() throws TraktApiException {
        return getMovieHistory().stream().map(o -> o.getMovie()).toList();
    }

    public void setLastMovieSync(Date lastMovieSync) {
        this.historyManager.setLastMovieSync(lastMovieSync);
    }
}
