package com.williamwigemo;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.williamwigemo.spotify.SpotifyAPI;
import com.williamwigemo.spotify.SpotifyApiException;
import com.williamwigemo.spotify.SpotifyPlaylistSync;
import com.williamwigemo.spotify.SpotifyTrack;
import com.williamwigemo.trakt.TraktApi;
import com.williamwigemo.trakt.TraktApiException;
import com.williamwigemo.trakt.TraktMovie;

import me.tongfei.progressbar.ProgressBar;

public class App {

    private final SpotifyAPI spotifyAPI;
    private final TraktApi traktApi;

    public App() throws IOException {
        this.spotifyAPI = new SpotifyAPI();
        this.traktApi = new TraktApi();
    }

    private Map<TraktMovie, Set<SpotifyTrack>> loadSoundtracks(List<TraktMovie> history, Set<String> soundtrackNames)
            throws IOException, SpotifyApiException {

        ImdbSoundtrackFetcher imdbSoundtrackFetcher = new ImdbSoundtrackFetcher();
        Map<String, Set<String>> imdbIdToTrackNames = new HashMap<>();
        Map<String, TraktMovie> imdbIdToTraktMovie = new HashMap<>();

        // for each movie, fetch soundtracks
        for (TraktMovie movie : ProgressBar.wrap(history,
                "Fetching soundtracks from " + history.size() + " movies")) {
            String imdbId = movie.ids.imdb;
            List<String> trackNames = imdbSoundtrackFetcher.getSoundtracks(imdbId).stream()
                    .map(o -> o.title).toList();
            soundtrackNames.addAll(trackNames);
            imdbIdToTrackNames.put(imdbId, trackNames.stream().collect(Collectors.toSet()));
            imdbIdToTraktMovie.put(imdbId, movie);
        }

        Map<TraktMovie, Set<SpotifyTrack>> imdbIdToTracks = new HashMap<>();

        for (String imdbId : ProgressBar.wrap(imdbIdToTrackNames.keySet(), "Fetching Spotify tracks...")) {
            Set<SpotifyTrack> tracks = new HashSet<>();

            // for each soundtrack name, get the Spotify uri
            for (String trackName : imdbIdToTrackNames.get(imdbId)) {
                Optional<SpotifyTrack> track = this.spotifyAPI.getTrackByName(trackName);
                if (track.isPresent()) {
                    tracks.add(track.get());
                }
            }
            imdbIdToTracks.put(imdbIdToTraktMovie.get(imdbId), tracks);
        }

        return imdbIdToTracks;
    }

    private void displaySummary(List<TraktMovie> history, Map<TraktMovie, Set<SpotifyTrack>> movieToTracksMap,
            Set<SpotifyTrack> allTracksAdded) {
        // print summary
        System.out.println("Tracks added: " + allTracksAdded.size());
        System.out.println("Number of movies: " + history.size());
        System.out.println();

        for (TraktMovie movie : movieToTracksMap.keySet()) {
            Set<SpotifyTrack> tracks = movieToTracksMap.get(movie);
            Set<SpotifyTrack> tracksAdded = new HashSet<>();

            for (SpotifyTrack track : tracks) {
                if (allTracksAdded.contains(track)) {
                    tracksAdded.add(track);
                }
            }

            if (tracksAdded.size() == 0) {
                continue;
            }

            System.out.print(movie.title + " (" + movie.year + "): ");

            System.out.println("Added " + tracksAdded.size() + " tracks");

            for (SpotifyTrack track : tracksAdded) {
                String artists = String.join(", ", track.artists.stream().limit(3).map(o -> o.name).toList());
                System.out.println("   - " + artists + ": " + track.name);
            }
            System.out.println();
        }
    }

    public void run() {
        try {
            this.traktApi.authenticate();
        } catch (TraktApiException e) {
            System.out.println("Failed to authenticate with Trakt.");
            e.printStackTrace();
        }
        this.spotifyAPI.authenticate();

        // load watched movies
        List<TraktMovie> history;
        try {
            history = this.traktApi.getMovieHistory();
            history = history.stream().limit(5).toList();
        } catch (TraktApiException e) {
            System.out.println("Failed to load movie history from Trakt.");
            e.printStackTrace();
            return;
        }

        // load soundtracks to respective movie
        Set<String> soundtrackNames = new HashSet<>();

        Map<TraktMovie, Set<SpotifyTrack>> movieToTracksMap;
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
        Set<SpotifyTrack> tracksAdded;
        try {
            tracksAdded = playlistSync.updateMovies(movieToTracksMap);
        } catch (SpotifyApiException e) {
            System.out.println("Failed adding soundtracks to Spotify playlist.");
            e.printStackTrace();
            return;
        }

        displaySummary(history, movieToTracksMap, tracksAdded);
    }

    public static void main(String[] args) throws IOException, SpotifyApiException, TraktApiException {
        App app = new App();
        app.run();
    }
}
