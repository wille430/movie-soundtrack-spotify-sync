package com.williamwigemo;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.williamwigemo.entities.MediaEntity;
import com.williamwigemo.entities.SpotifyTrackEntity;
import com.williamwigemo.spotify.SpotifyAPI;
import com.williamwigemo.spotify.SpotifyApiException;
import com.williamwigemo.spotify.SpotifyPlaylistSync;
import com.williamwigemo.trakt.TraktApi;
import com.williamwigemo.trakt.TraktApiException;

import me.tongfei.progressbar.ProgressBar;

public class App {

    private final SpotifyAPI spotifyAPI;
    private final TraktApi traktApi;
    private final AppCredentials appCredentials;
    private final MediaService mediaService;

    public App() throws IOException {
        this.appCredentials = new AppCredentials("app.properties");
        this.spotifyAPI = new SpotifyAPI(this.appCredentials.getSpotifyClientId());
        this.traktApi = new TraktApi(this.appCredentials);
        this.mediaService = new MediaService(this.spotifyAPI);
    }

    private Map<MediaEntity, Set<SpotifyTrackEntity>> loadSoundtracks(List<MediaEntity> history,
            Set<String> soundtrackNames)
            throws IOException, SpotifyApiException {

        Map<MediaEntity, Set<SpotifyTrackEntity>> imdbIdToTracks = new HashMap<>();
        for (MediaEntity entity : ProgressBar.wrap(history, "Fetching soundtracks...")) {
            entity = this.mediaService.fetchMedia(entity);
            imdbIdToTracks.put(entity, entity.getSoundtracks());
        }

        return imdbIdToTracks;
    }

    private void displaySummary(List<MediaEntity> history, Map<MediaEntity, Set<SpotifyTrackEntity>> movieToTracksMap,
            Set<SpotifyTrackEntity> allTracksAdded) {
        // print summary
        System.out.println("Tracks added: " + allTracksAdded.size());
        System.out.println("Number of movies: " + history.size());
        System.out.println();

        for (MediaEntity movie : movieToTracksMap.keySet()) {
            Set<SpotifyTrackEntity> tracks = movieToTracksMap.get(movie);
            Set<SpotifyTrackEntity> tracksAdded = new HashSet<>();

            for (SpotifyTrackEntity track : tracks) {
                if (allTracksAdded.contains(track)) {
                    tracksAdded.add(track);
                }
            }

            if (tracksAdded.size() == 0) {
                continue;
            }

            System.out.print(movie.getTitle() + " (" + movie.getYear() + "): ");

            System.out.println("Added " + tracksAdded.size() + " tracks");

            for (SpotifyTrackEntity track : tracksAdded) {
                String collaborators = String.join(", ", track.getCollaborators().stream().limit(3).toList());
                System.out.println("   - " + collaborators + ": " + track.getTrackName());
            }
            System.out.println();
        }
    }

    public void run() {
        Date lastMovieSync = new Date();

        try {
            this.traktApi.authenticate();
        } catch (TraktApiException e) {
            System.out.println("Failed to authenticate with Trakt.");
            e.printStackTrace();
        }
        this.spotifyAPI.authenticate();

        // load watched movies
        List<MediaEntity> history;
        try {
            history = this.traktApi.getWatchedMovies().stream().map(o -> o.toEntity()).toList();
        } catch (TraktApiException e) {
            System.out.println("Failed to load movie history from Trakt.");
            e.printStackTrace();
            return;
        }

        // load soundtracks to respective movie
        Set<String> soundtrackNames = new HashSet<>();

        Map<MediaEntity, Set<SpotifyTrackEntity>> movieToTracksMap;
        try {
            movieToTracksMap = loadSoundtracks(history, soundtrackNames);
        } catch (SpotifyApiException | IOException e) {
            System.out.println("Failed to load soundtracks from Spotify.");
            e.printStackTrace();
            return;
        }

        // create/update spotify playlist
        SpotifyPlaylistSync playlistSync = new SpotifyPlaylistSync(spotifyAPI);

        System.out.println("Populating playlist with new soundtracks...");
        Set<SpotifyTrackEntity> tracksAdded;
        try {
            tracksAdded = playlistSync.updateMovies(movieToTracksMap);
        } catch (SpotifyApiException e) {
            System.out.println("Failed adding soundtracks to Spotify playlist.");
            e.printStackTrace();
            return;
        }

        this.traktApi.setLastMovieSync(lastMovieSync);

        displaySummary(history, movieToTracksMap, tracksAdded);
    }

    public static void main(String[] args) throws IOException, SpotifyApiException, TraktApiException {
        App app = new App();
        app.run();
    }
}
